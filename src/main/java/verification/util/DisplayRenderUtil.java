package verification.util;

import ta.ota.DOTA;
import ta.ota.LogicTimeWord;
import ta.ota.ResetLogicTimeWord;
import ta.timedaction.ResetTimedAction;
import ta.timedaction.TimedAction;
import ta.timedword.TimedWord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Render helpers for human-readable console output.
 * Internal model symbols remain unchanged.
 */
public final class DisplayRenderUtil {
    private DisplayRenderUtil() {
    }

    public static String renderAction(String action) {
        return DisplayAliasContext.aliasAction(action);
    }

    public static String renderResetWord(ResetLogicTimeWord word) {
        return renderTimedWord(word);
    }

    public static String renderLogicWord(LogicTimeWord word) {
        return renderTimedWord(word);
    }

    public static String renderHypothesis(DOTA hypothesis) {
        if (hypothesis == null) {
            return "null";
        }
        return renderText(hypothesis.toString());
    }

    public static String renderText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        Map<String, String> aliasMap = DisplayAliasContext.aliasMap();
        if (aliasMap.isEmpty()) {
            return text;
        }
        List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>(aliasMap.entrySet());
        entries.sort(new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> a, Map.Entry<String, String> b) {
                return Integer.compare(b.getKey().length(), a.getKey().length());
            }
        });

        String rendered = text;
        for (Map.Entry<String, String> entry : entries) {
            String physical = entry.getKey();
            String logical = entry.getValue();
            rendered = rendered.replace("\"" + physical + "\"", "\"" + logical + "\"");
            rendered = rendered.replace("(" + physical + ",", "(" + logical + ",");
        }
        return rendered;
    }

    private static String renderTimedWord(TimedWord<? extends TimedAction> word) {
        if (word == null || word.isEmpty()) {
            return "empty";
        }
        StringBuilder sb = new StringBuilder();
        for (TimedAction action : word.getTimedActions()) {
            String symbol = renderAction(action.getSymbol());
            if (action instanceof ResetTimedAction) {
                ResetTimedAction resetAction = (ResetTimedAction) action;
                sb.append("(")
                        .append(symbol)
                        .append(",")
                        .append(action.getValue())
                        .append(",")
                        .append(resetAction.isReset() ? "r" : "n")
                        .append(")");
            } else {
                sb.append("(")
                        .append(symbol)
                        .append(",")
                        .append(action.getValue())
                        .append(")");
            }
        }
        return sb.toString();
    }
}

