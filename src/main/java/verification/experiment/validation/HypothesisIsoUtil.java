package verification.experiment.validation;

import verification.util.PortActionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class HypothesisIsoUtil {
    private static final Pattern INIT_PATTERN = Pattern.compile("\\\"init\\\"\\s*:\\s*(\\d+)");
    private static final Pattern STATE_LIST_PATTERN = Pattern.compile("\\\"s\\\"\\s*:\\s*\\[([^]]*)]\\s*");
    private static final Pattern ACCEPTED_LIST_PATTERN = Pattern.compile("\\\"accpted\\\"\\s*:\\s*\\[([^]]*)]\\s*");
    private static final Pattern TRANSITION_PATTERN = Pattern.compile(
            "\\\"\\d+\\\"\\s*:\\s*\\[(\\d+)\\s*,\\s*\\\"([^\\\"]+)\\\"\\s*,\\s*\\\"([^\\\"]+)\\\"\\s*,\\s*(\\d+)\\s*,\\s*([rn])\\s*]\\s*,?");

    private HypothesisIsoUtil() {
    }

    static IsoResult compare(String originalHypothesis, String channelHypothesis) {
        if (isBlank(originalHypothesis) || isBlank(channelHypothesis)) {
            return IsoResult.fail("最终假设为空，无法做同构对比");
        }

        ParsedAutomaton orig = parse(originalHypothesis);
        ParsedAutomaton chan = parse(channelHypothesis);
        if (!orig.parseErrors.isEmpty() || !chan.parseErrors.isEmpty()) {
            return IsoResult.fail("解析失败: orig=" + orig.parseErrors + ", chan=" + chan.parseErrors);
        }

        String preCheckError = preCheck(orig, chan);
        if (preCheckError != null) {
            return IsoResult.fail(preCheckError);
        }

        Map<Integer, Integer> mapping = new HashMap<Integer, Integer>();
        Map<Integer, Integer> reverse = new HashMap<Integer, Integer>();
        mapping.put(orig.init, chan.init);
        reverse.put(chan.init, orig.init);

        boolean ok = dfs(orig, chan, mapping, reverse);
        if (!ok) {
            return IsoResult.fail("存在结构差异，无法建立状态双射");
        }
        return IsoResult.pass("同构，状态映射大小=" + mapping.size());
    }

    private static boolean dfs(ParsedAutomaton orig,
                               ParsedAutomaton chan,
                               Map<Integer, Integer> mapping,
                               Map<Integer, Integer> reverse) {
        if (!propagate(orig, chan, mapping, reverse)) {
            return false;
        }
        if (mapping.size() == orig.states.size()) {
            return reverse.size() == chan.states.size();
        }

        int nextOrig = pickUnmapped(orig.states, mapping);
        List<Integer> candidates = new ArrayList<Integer>();
        for (Integer candidate : chan.states) {
            if (reverse.containsKey(candidate)) {
                continue;
            }
            if (!localCompatible(orig, chan, nextOrig, candidate)) {
                continue;
            }
            candidates.add(candidate);
        }
        Collections.sort(candidates);

        for (Integer candidate : candidates) {
            Map<Integer, Integer> nextMapping = new HashMap<Integer, Integer>(mapping);
            Map<Integer, Integer> nextReverse = new HashMap<Integer, Integer>(reverse);
            nextMapping.put(nextOrig, candidate);
            nextReverse.put(candidate, nextOrig);
            if (dfs(orig, chan, nextMapping, nextReverse)) {
                mapping.clear();
                mapping.putAll(nextMapping);
                reverse.clear();
                reverse.putAll(nextReverse);
                return true;
            }
        }
        return false;
    }

    private static int pickUnmapped(Set<Integer> states, Map<Integer, Integer> mapping) {
        List<Integer> sorted = new ArrayList<Integer>(states);
        Collections.sort(sorted);
        for (Integer s : sorted) {
            if (!mapping.containsKey(s)) {
                return s;
            }
        }
        return -1;
    }

    private static boolean propagate(ParsedAutomaton orig,
                                     ParsedAutomaton chan,
                                     Map<Integer, Integer> mapping,
                                     Map<Integer, Integer> reverse) {
        boolean changed = true;
        while (changed) {
            changed = false;
            List<Map.Entry<Integer, Integer>> snapshot = new ArrayList<Map.Entry<Integer, Integer>>(mapping.entrySet());
            for (Map.Entry<Integer, Integer> entry : snapshot) {
                int so = entry.getKey();
                int sc = entry.getValue();
                if (!localCompatible(orig, chan, so, sc)) {
                    return false;
                }

                Map<String, Edge> outO = orig.outgoing.get(so);
                Map<String, Edge> outC = chan.outgoing.get(sc);
                if (outO == null || outC == null) {
                    return false;
                }
                if (outO.size() != outC.size() || !outO.keySet().equals(outC.keySet())) {
                    return false;
                }

                for (String label : outO.keySet()) {
                    Edge eo = outO.get(label);
                    Edge ec = outC.get(label);
                    int to = eo.target;
                    int tc = ec.target;

                    Integer mapped = mapping.get(to);
                    if (mapped != null) {
                        if (mapped != tc) {
                            return false;
                        }
                        continue;
                    }
                    Integer reversed = reverse.get(tc);
                    if (reversed != null && reversed != to) {
                        return false;
                    }
                    if (!localCompatible(orig, chan, to, tc)) {
                        return false;
                    }
                    mapping.put(to, tc);
                    reverse.put(tc, to);
                    changed = true;
                }
            }
        }
        return true;
    }

    private static boolean localCompatible(ParsedAutomaton orig,
                                           ParsedAutomaton chan,
                                           int so,
                                           int sc) {
        if (orig.accepted.contains(so) != chan.accepted.contains(sc)) {
            return false;
        }
        String sigO = orig.signature.get(so);
        String sigC = chan.signature.get(sc);
        if (sigO == null || sigC == null) {
            return false;
        }
        return sigO.equals(sigC);
    }

    private static String preCheck(ParsedAutomaton orig, ParsedAutomaton chan) {
        if (orig.init < 0 || chan.init < 0) {
            return "初态解析失败";
        }
        if (orig.states.size() != chan.states.size()) {
            return "状态数量不一致: orig=" + orig.states.size() + ", chan=" + chan.states.size();
        }
        if (orig.transitions.size() != chan.transitions.size()) {
            return "迁移数量不一致: orig=" + orig.transitions.size() + ", chan=" + chan.transitions.size();
        }
        if (orig.accepted.size() != chan.accepted.size()) {
            return "接受态数量不一致: orig=" + orig.accepted.size() + ", chan=" + chan.accepted.size();
        }
        Set<String> labelO = new HashSet<String>(orig.transitionLabels);
        Set<String> labelC = new HashSet<String>(chan.transitionLabels);
        if (!labelO.equals(labelC)) {
            return "迁移标签集合不一致: orig=" + labelO + ", chan=" + labelC;
        }
        return null;
    }

    private static ParsedAutomaton parse(String text) {
        ParsedAutomaton parsed = new ParsedAutomaton();
        String[] lines = text.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            Matcher initMatcher = INIT_PATTERN.matcher(line);
            if (initMatcher.find()) {
                parsed.init = parseIntSafe(initMatcher.group(1), parsed, "init");
                continue;
            }

            Matcher statesMatcher = STATE_LIST_PATTERN.matcher(line);
            if (statesMatcher.find()) {
                parsed.states.addAll(parseIntList(statesMatcher.group(1), parsed, "states"));
                continue;
            }

            Matcher acceptedMatcher = ACCEPTED_LIST_PATTERN.matcher(line);
            if (acceptedMatcher.find()) {
                parsed.accepted.addAll(parseIntList(acceptedMatcher.group(1), parsed, "accepted"));
                continue;
            }

            Matcher tranMatcher = TRANSITION_PATTERN.matcher(line);
            if (tranMatcher.find()) {
                int source = parseIntSafe(tranMatcher.group(1), parsed, "transition-source");
                String symbol = normalizeSymbol(tranMatcher.group(2));
                String guard = normalizeGuard(tranMatcher.group(3));
                int target = parseIntSafe(tranMatcher.group(4), parsed, "transition-target");
                boolean reset = "r".equalsIgnoreCase(tranMatcher.group(5));
                Edge edge = new Edge(source, target, symbol, guard, reset);
                parsed.transitions.add(edge);
                parsed.states.add(source);
                parsed.states.add(target);
                parsed.transitionLabels.add(edge.label);
            }
        }

        if (!parsed.states.contains(parsed.init) && parsed.init >= 0) {
            parsed.states.add(parsed.init);
        }
        parsed.buildOutgoingAndSignature();
        return parsed;
    }

    private static int parseIntSafe(String value, ParsedAutomaton parsed, String phase) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            parsed.parseErrors.add(phase + ":" + value);
            return -1;
        }
    }

    private static Set<Integer> parseIntList(String body, ParsedAutomaton parsed, String phase) {
        Set<Integer> result = new HashSet<Integer>();
        if (body == null || body.trim().isEmpty()) {
            return result;
        }
        String[] parts = body.split(",");
        for (String part : parts) {
            String token = part.trim();
            if (token.isEmpty()) {
                continue;
            }
            result.add(parseIntSafe(token, parsed, phase));
        }
        return result;
    }

    private static String normalizeSymbol(String symbol) {
        return PortActionUtil.stripSuffix(symbol.trim());
    }

    private static String normalizeGuard(String guard) {
        return guard == null ? "" : guard.replace(" ", "");
    }

    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    static final class IsoResult {
        final boolean isomorphic;
        final String message;

        private IsoResult(boolean isomorphic, String message) {
            this.isomorphic = isomorphic;
            this.message = message;
        }

        static IsoResult pass(String message) {
            return new IsoResult(true, message);
        }

        static IsoResult fail(String message) {
            return new IsoResult(false, message);
        }
    }

    private static final class Edge {
        final int source;
        final int target;
        final String symbol;
        final String guard;
        final boolean reset;
        final String label;

        private Edge(int source, int target, String symbol, String guard, boolean reset) {
            this.source = source;
            this.target = target;
            this.symbol = symbol;
            this.guard = guard;
            this.reset = reset;
            this.label = symbol + "|" + guard + "|" + (reset ? "r" : "n");
        }
    }

    private static final class ParsedAutomaton {
        int init = -1;
        final Set<Integer> states = new HashSet<Integer>();
        final Set<Integer> accepted = new HashSet<Integer>();
        final List<Edge> transitions = new ArrayList<Edge>();
        final Set<String> transitionLabels = new HashSet<String>();
        final List<String> parseErrors = new ArrayList<String>();

        final Map<Integer, Map<String, Edge>> outgoing = new HashMap<Integer, Map<String, Edge>>();
        final Map<Integer, String> signature = new HashMap<Integer, String>();

        void buildOutgoingAndSignature() {
            for (Integer state : states) {
                outgoing.put(state, new HashMap<String, Edge>());
            }
            for (Edge edge : transitions) {
                Map<String, Edge> edgeMap = outgoing.get(edge.source);
                if (edgeMap == null) {
                    edgeMap = new HashMap<String, Edge>();
                    outgoing.put(edge.source, edgeMap);
                }
                // DOTA should be deterministic on (symbol, guard, reset).
                if (edgeMap.containsKey(edge.label)) {
                    parseErrors.add("duplicate-edge-label@state=" + edge.source + ":" + edge.label);
                }
                edgeMap.put(edge.label, edge);
            }

            for (Integer state : states) {
                Map<String, Integer> countByLabel = new HashMap<String, Integer>();
                Map<String, Edge> out = outgoing.get(state);
                if (out != null) {
                    for (String label : out.keySet()) {
                        Integer old = countByLabel.get(label);
                        countByLabel.put(label, old == null ? 1 : old + 1);
                    }
                }
                List<String> sorted = new ArrayList<String>();
                for (Map.Entry<String, Integer> entry : countByLabel.entrySet()) {
                    sorted.add(entry.getKey() + "#" + entry.getValue());
                }
                Collections.sort(sorted);
                StringBuilder sb = new StringBuilder();
                sb.append(accepted.contains(state) ? "A" : "N").append('|');
                for (String item : sorted) {
                    sb.append(item).append(';');
                }
                signature.put(state, sb.toString());
            }
        }
    }
}
