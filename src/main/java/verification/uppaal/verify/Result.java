package verification.uppaal.verify;

import lombok.AllArgsConstructor;
import lombok.Data;
import ta.TimeGuard;
import ta.dbm.ActionGuard;
import ta.ota.LogicTimeWord;
import ta.ota.LogicTimedAction;

import java.util.regex.Pattern;

@Data
@AllArgsConstructor
public class Result {
    private static final Pattern ANSI_ESCAPE_PATTERN = Pattern.compile("\\u001B\\[[;\\d]*[ -/]*[@-~]");

    private boolean satisfy;
    private String content;
    private LogicTimeWord logicTimeWord;

    Result(String pattern) {
        this.content = pattern;
        boolean notSatisfied = content.contains("Formula is NOT satisfied");
        boolean satisfy = content.contains("Formula is satisfied");
        if (!satisfy && !notSatisfied) {
            throw new RuntimeException("性质验证出错\n" + pattern);
        }
        this.satisfy = satisfy;
        logicTimeWord = LogicTimeWord.emptyWord();
        String[] strs = content.split("\n");
        boolean inTransitionStage = false;
        boolean inStateStage = false;
        String lastAssumeTime = "";
        for (String raw : strs) {
            String str = sanitizeLine(raw);
            if (str.startsWith("Transition:") || str.startsWith("Transitions:")) {
                inTransitionStage = true;
                inStateStage = false;
                continue;
            }
            if (str.startsWith("State:")) {
                inTransitionStage = false;
                inStateStage = true;
                continue;
            }
            if (!inTransitionStage && !inStateStage) {
                continue;
            }

            str = str.trim();
            if (inStateStage && str.contains("assume.x=")) {
                lastAssumeTime = str;
                continue;
            }
            if (inTransitionStage && str.startsWith("assume.")) {
                if (lastAssumeTime.isEmpty()) {
                    throw new IllegalStateException("Counterexample parse error: missing state clock value before transition line: " + str);
                }
                String symbol = parserExactTrans(str);
                if (symbol == null) {
                    throw new IllegalStateException("Counterexample parse error: cannot extract transition symbol from line: " + str);
                }
                LogicTimedAction action = new LogicTimedAction(symbol, parserTimedAction(lastAssumeTime));
                logicTimeWord = logicTimeWord.concat(action);
                inTransitionStage = false;
            }
        }
    }

    private static Double parserTimedAction(String str) {
        String[] strs = str.split(" ");
        String target = null;
        for (String s : strs) {
            if (s.startsWith("assume.x=")) {
                target = s.replace("assume.x=", "");
                break;
            }
        }
        if (target == null || target.trim().isEmpty()) {
            throw new IllegalStateException("Counterexample parse error: state line does not contain assume.x value: " + str);
        }
        return Double.valueOf(target);
    }

    private static String sanitizeLine(String line) {
        String cleaned = ANSI_ESCAPE_PATTERN.matcher(line).replaceAll("");
        cleaned = cleaned.replace("\u001B", "");
        return cleaned.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
    }

    /**
     * Parse one Uppaal transition line and extract synchronization symbol.
     */
    private static String parserExactTrans(String transLineStr) {
        int leftIdx = transLineStr.indexOf("{");
        if (leftIdx == -1) {
            return null;
        }
        transLineStr = transLineStr.substring(leftIdx + 1);
        String[] splitStrs = transLineStr.split(",");
        if (splitStrs.length < 2) {
            return null;
        }
        return splitStrs[1].trim();
    }

    /**
     * Convert Uppaal transition text into an ActionGuard instance.
     */
    private static ActionGuard parserTransition(String transLineStr) {
        int leftIdx = transLineStr.indexOf("{");
        if (leftIdx == -1) {
            return null;
        }
        transLineStr = transLineStr.substring(leftIdx + 1);
        String[] splitStrs = transLineStr.split(",");
        String timeStr = splitStrs[0].trim();
        String symbolStr = splitStrs[1].trim();
        symbolStr = symbolStr.substring(0, symbolStr.length() - 1);
        return new ActionGuard(symbolStr, parserTimeGuard(timeStr));
    }

    /**
     * Parse a textual time guard (single bound form only).
     */
    private static TimeGuard parserTimeGuard(String timeGuardStr) {
        timeGuardStr = timeGuardStr.replace(" ", "");
        TimeGuard timeGuard = new TimeGuard();
        int numberIdx = 2;
        if (timeGuardStr.charAt(2) == '=') {
            numberIdx = 3;
        }
        switch (timeGuardStr.charAt(1)) {
            case '>':
                timeGuard.setLowerBoundOpen(timeGuardStr.charAt(2) != '=');
                timeGuard.setLowerBound(timeGuardStr.charAt(numberIdx) - '0');
                timeGuard.setUpperBoundOpen(true);
                timeGuard.setUpperBound(TimeGuard.MAX_TIME);
                break;
            case '<':
                timeGuard.setLowerBoundOpen(false);
                timeGuard.setLowerBound(0);
                timeGuard.setUpperBoundOpen(timeGuardStr.charAt(2) != '=');
                timeGuard.setUpperBound(timeGuardStr.charAt(numberIdx) - '0');
                break;
            default:
                System.out.println("Unexpected time guard format: " + timeGuardStr);
        }

        return timeGuard;
    }
}
