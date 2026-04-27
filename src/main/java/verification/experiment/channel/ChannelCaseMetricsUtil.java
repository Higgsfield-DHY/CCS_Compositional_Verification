package verification.experiment.channel;

import verification.experiment.Experiment;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.uppaal.model.UppaalTransition;
import verification.uppaal.model.label.AssignmentLabel;
import verification.uppaal.model.label.GuardLabel;
import verification.uppaal.model.label.SynchronizedLabel;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChannelCaseMetricsUtil {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final List<String> COMPARATORS = Arrays.asList("<=", ">=", "==", "<", ">");

    private ChannelCaseMetricsUtil() {
    }

    public static CaseMetrics analyze(Experiment experiment) throws IOException {
        List<Template> m2 = experiment.getM2();
        List<TemplateMetrics> templateMetrics = new ArrayList<TemplateMetrics>();
        BigInteger product = BigInteger.ONE;
        int totalClockCount = 0;
        Set<String> alphabet = new TreeSet<String>();
        for (Template template : m2) {
            TemplateMetrics metric = analyzeTemplate(template);
            templateMetrics.add(metric);
            product = product.multiply(BigInteger.valueOf(metric.localStateSpace));
            totalClockCount += metric.clockCount;
            alphabet.addAll(metric.syncLabels);
        }
        return new CaseMetrics(product, totalClockCount, alphabet.size(), templateMetrics, alphabet);
    }

    public static TemplateMetrics analyzeTemplate(Template template) {
        IntModel model = IntModel.from(template);
        int stateCount = computeReachableDiscreteStateCount(template, model);
        int locationCount = template.getUppaalLocationList() == null ? 0 : template.getUppaalLocationList().size();
        int clockCount = model.clockNames.size();
        Set<String> syncLabels = collectSyncLabels(template);
        return new TemplateMetrics(template.getName(), stateCount, locationCount, clockCount,
                new ArrayList<String>(model.intVarOrder), syncLabels);
    }

    private static int computeReachableDiscreteStateCount(Template template, IntModel model) {
        if (template.getInitUppaalLocation() == null) {
            return 0;
        }
        LocalState init = new LocalState(template.getInitUppaalLocation().getId(), model.initialValues);
        Set<LocalState> visited = new LinkedHashSet<LocalState>();
        Deque<LocalState> queue = new ArrayDeque<LocalState>();
        visited.add(init);
        queue.add(init);
        List<UppaalTransition> transitions = template.getUppaalTransitionList() == null
                ? Collections.<UppaalTransition>emptyList()
                : template.getUppaalTransitionList();
        while (!queue.isEmpty()) {
            LocalState current = queue.removeFirst();
            for (UppaalTransition transition : transitions) {
                if (transition.getSource() == null || transition.getTarget() == null) {
                    continue;
                }
                if (!transition.getSource().getId().equals(current.locationId)) {
                    continue;
                }
                if (!guardSatisfied(transition.getGuardLabel(), current.values, model)) {
                    continue;
                }
                Map<String, Integer> nextValues = applyAssignments(transition.getAssignmentLabel(), current.values, model);
                LocalState next = new LocalState(transition.getTarget().getId(), nextValues);
                if (visited.add(next)) {
                    queue.addLast(next);
                }
            }
        }
        return visited.size();
    }

    private static boolean guardSatisfied(GuardLabel guardLabel, Map<String, Integer> env, IntModel model) {
        if (guardLabel == null || guardLabel.getText() == null || guardLabel.getText().trim().isEmpty()) {
            return true;
        }
        String[] parts = guardLabel.getText().split("&&");
        for (String raw : parts) {
            String clause = raw.trim();
            if (clause.isEmpty()) {
                continue;
            }
            if (!isIntRelevant(clause, model)) {
                continue;
            }
            if (!evaluateComparison(clause, env)) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, Integer> applyAssignments(AssignmentLabel assignmentLabel,
                                                         Map<String, Integer> env,
                                                         IntModel model) {
        if (assignmentLabel == null || assignmentLabel.getText() == null || assignmentLabel.getText().trim().isEmpty()) {
            return env;
        }
        Map<String, Integer> base = new LinkedHashMap<String, Integer>(env);
        Map<String, Integer> updates = new LinkedHashMap<String, Integer>();
        String[] pieces = assignmentLabel.getText().split(",");
        for (String raw : pieces) {
            String clause = raw.trim();
            if (clause.isEmpty()) {
                continue;
            }
            if (clause.endsWith("++")) {
                String lhs = clause.substring(0, clause.length() - 2).trim();
                if (model.clockNames.contains(lhs) || !base.containsKey(lhs)) {
                    continue;
                }
                updates.put(lhs, base.get(lhs) + 1);
                continue;
            }
            if (clause.endsWith("--")) {
                String lhs = clause.substring(0, clause.length() - 2).trim();
                if (model.clockNames.contains(lhs) || !base.containsKey(lhs)) {
                    continue;
                }
                updates.put(lhs, base.get(lhs) - 1);
                continue;
            }
            int index = clause.indexOf('=');
            if (index < 0) {
                continue;
            }
            String lhs = clause.substring(0, index).trim();
            String rhs = clause.substring(index + 1).trim();
            if (model.clockNames.contains(lhs) || !base.containsKey(lhs)) {
                continue;
            }
            updates.put(lhs, evaluateArithmetic(rhs, base));
        }
        if (updates.isEmpty()) {
            return env;
        }
        Map<String, Integer> result = new LinkedHashMap<String, Integer>(base);
        result.putAll(updates);
        return result;
    }

    private static boolean isIntRelevant(String clause, IntModel model) {
        Matcher matcher = IDENTIFIER.matcher(clause);
        boolean hasInt = false;
        while (matcher.find()) {
            String symbol = matcher.group();
            if (model.clockNames.contains(symbol)) {
                return false;
            }
            if (model.initialValues.containsKey(symbol)) {
                hasInt = true;
            }
        }
        return hasInt;
    }

    private static boolean evaluateComparison(String clause, Map<String, Integer> env) {
        for (String comparator : COMPARATORS) {
            int index = clause.indexOf(comparator);
            if (index < 0) {
                continue;
            }
            int left = evaluateArithmetic(clause.substring(0, index).trim(), env);
            int right = evaluateArithmetic(clause.substring(index + comparator.length()).trim(), env);
            if ("<=".equals(comparator)) {
                return left <= right;
            }
            if (">=".equals(comparator)) {
                return left >= right;
            }
            if ("==".equals(comparator)) {
                return left == right;
            }
            if ("<".equals(comparator)) {
                return left < right;
            }
            if (">".equals(comparator)) {
                return left > right;
            }
        }
        throw new IllegalStateException("Unsupported integer guard clause: " + clause);
    }

    private static int evaluateArithmetic(String expression, Map<String, Integer> env) {
        return new ArithmeticParser(expression, env).parse();
    }

    private static Set<String> collectSyncLabels(Template template) {
        Set<String> labels = new TreeSet<String>();
        if (template.getUppaalTransitionList() == null) {
            return labels;
        }
        for (UppaalTransition transition : template.getUppaalTransitionList()) {
            SynchronizedLabel label = transition.getSynchronizedLabel();
            if (label == null || label.getText() == null) {
                continue;
            }
            String text = label.getText().trim();
            if (text.isEmpty()) {
                continue;
            }
            char last = text.charAt(text.length() - 1);
            if (last == '!' || last == '?') {
                text = text.substring(0, text.length() - 1);
            }
            labels.add(text);
        }
        return labels;
    }

    public static final class CaseMetrics {
        public final BigInteger m2LocalStateSpace;
        public final int m2ClockCount;
        public final int m2AlphabetSize;
        public final List<TemplateMetrics> templateMetrics;
        public final Set<String> m2Alphabet;

        public CaseMetrics(BigInteger m2LocalStateSpace,
                           int m2ClockCount,
                           int m2AlphabetSize,
                           List<TemplateMetrics> templateMetrics,
                           Set<String> m2Alphabet) {
            this.m2LocalStateSpace = m2LocalStateSpace;
            this.m2ClockCount = m2ClockCount;
            this.m2AlphabetSize = m2AlphabetSize;
            this.templateMetrics = templateMetrics;
            this.m2Alphabet = m2Alphabet;
        }
    }

    public static final class TemplateMetrics {
        public final String templateName;
        public final int localStateSpace;
        public final int locationCount;
        public final int clockCount;
        public final List<String> intVariables;
        public final Set<String> syncLabels;

        public TemplateMetrics(String templateName,
                               int localStateSpace,
                               int locationCount,
                               int clockCount,
                               List<String> intVariables,
                               Set<String> syncLabels) {
            this.templateName = templateName;
            this.localStateSpace = localStateSpace;
            this.locationCount = locationCount;
            this.clockCount = clockCount;
            this.intVariables = intVariables;
            this.syncLabels = syncLabels;
        }
    }

    private static final class IntModel {
        private final List<String> intVarOrder;
        private final Map<String, Integer> initialValues;
        private final Set<String> clockNames;

        private IntModel(List<String> intVarOrder, Map<String, Integer> initialValues, Set<String> clockNames) {
            this.intVarOrder = intVarOrder;
            this.initialValues = initialValues;
            this.clockNames = clockNames;
        }

        static IntModel from(Template template) {
            List<String> intVarOrder = new ArrayList<String>();
            Map<String, Integer> initialValues = new LinkedHashMap<String, Integer>();
            Set<String> clockNames = new LinkedHashSet<String>();
            Declaration local = template.getLocalDeclaration();
            if (local != null && local.isMapInitialized()) {
                for (Map.Entry<String, String> entry : local.getMap().entrySet()) {
                    String type = entry.getValue() == null ? "" : entry.getValue().trim();
                    for (String declarator : splitDeclarators(entry.getKey())) {
                        if ("clock".equals(type)) {
                            clockNames.add(declarator);
                        } else if ("int".equals(type)) {
                            String[] parsed = parseDeclarator(declarator);
                            intVarOrder.add(parsed[0]);
                            initialValues.put(parsed[0], Integer.parseInt(parsed[1]));
                        }
                    }
                }
            }
            return new IntModel(intVarOrder, initialValues, clockNames);
        }

        private static List<String> splitDeclarators(String rawKey) {
            if (rawKey == null || rawKey.trim().isEmpty()) {
                return Collections.emptyList();
            }
            String[] parts = rawKey.split(",");
            List<String> declarators = new ArrayList<String>();
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    declarators.add(trimmed);
                }
            }
            return declarators;
        }

        private static String[] parseDeclarator(String declarator) {
            int index = declarator.indexOf('=');
            if (index < 0) {
                return new String[]{declarator.trim(), "0"};
            }
            String name = declarator.substring(0, index).trim();
            String value = declarator.substring(index + 1).trim();
            return new String[]{name, value};
        }
    }

    private static final class LocalState {
        private final String locationId;
        private final Map<String, Integer> values;

        private LocalState(String locationId, Map<String, Integer> values) {
            this.locationId = locationId;
            this.values = new LinkedHashMap<String, Integer>(values);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LocalState)) {
                return false;
            }
            LocalState other = (LocalState) obj;
            return locationId.equals(other.locationId) && values.equals(other.values);
        }

        @Override
        public int hashCode() {
            return 31 * locationId.hashCode() + values.hashCode();
        }
    }

    private static final class ArithmeticParser {
        private final String text;
        private final Map<String, Integer> env;
        private int index;

        private ArithmeticParser(String text, Map<String, Integer> env) {
            this.text = text == null ? "" : text.replace(" ", "");
            this.env = env;
        }

        int parse() {
            int value = parseExpression();
            if (index != text.length()) {
                throw new IllegalStateException("Unexpected token in expression: " + text.substring(index));
            }
            return value;
        }

        private int parseExpression() {
            int value = parseTerm();
            while (index < text.length()) {
                char ch = text.charAt(index);
                if (ch == '+') {
                    index++;
                    value += parseTerm();
                } else if (ch == '-') {
                    index++;
                    value -= parseTerm();
                } else {
                    break;
                }
            }
            return value;
        }

        private int parseTerm() {
            int value = parseFactor();
            while (index < text.length()) {
                char ch = text.charAt(index);
                if (ch == '*') {
                    index++;
                    value *= parseFactor();
                } else if (ch == '/') {
                    index++;
                    value /= parseFactor();
                } else if (ch == '%') {
                    index++;
                    value %= parseFactor();
                } else {
                    break;
                }
            }
            return value;
        }

        private int parseFactor() {
            if (index >= text.length()) {
                throw new IllegalStateException("Unexpected end of arithmetic expression.");
            }
            char ch = text.charAt(index);
            if (ch == '(') {
                index++;
                int value = parseExpression();
                expect(')');
                return value;
            }
            if (ch == '+') {
                index++;
                return parseFactor();
            }
            if (ch == '-') {
                index++;
                return -parseFactor();
            }
            if (Character.isDigit(ch)) {
                return parseNumber();
            }
            return parseIdentifier();
        }

        private int parseNumber() {
            int start = index;
            while (index < text.length() && Character.isDigit(text.charAt(index))) {
                index++;
            }
            return Integer.parseInt(text.substring(start, index));
        }

        private int parseIdentifier() {
            int start = index;
            while (index < text.length()) {
                char ch = text.charAt(index);
                if (Character.isLetterOrDigit(ch) || ch == '_') {
                    index++;
                } else {
                    break;
                }
            }
            String name = text.substring(start, index);
            Integer value = env.get(name);
            if (value == null) {
                throw new IllegalStateException("Unknown integer symbol: " + name + " in " + text);
            }
            return value;
        }

        private void expect(char expected) {
            if (index >= text.length() || text.charAt(index) != expected) {
                throw new IllegalStateException("Expected '" + expected + "' in expression: " + text);
            }
            index++;
        }
    }
}
