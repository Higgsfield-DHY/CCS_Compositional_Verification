package verification.util;

public final class PortActionUtil {
    private static final String CQ2_HOOK_PREFIX = "__cq2h_";

    private PortActionUtil() {
    }

    public static String normalize(String action) {
        if (action == null) {
            return null;
        }
        return action.trim();
    }

    public static boolean isPortAction(String action) {
        action = normalize(action);
        if (action == null || action.length() < 2) {
            return false;
        }
        char suffix = action.charAt(action.length() - 1);
        return suffix == '!' || suffix == '?';
    }

    public static String channelOf(String action) {
        action = normalize(action);
        if (!isPortAction(action)) {
            return action;
        }
        return action.substring(0, action.length() - 1);
    }

    public static boolean isSend(String action) {
        action = normalize(action);
        return isPortAction(action) && action.charAt(action.length() - 1) == '!';
    }

    public static boolean isReceive(String action) {
        action = normalize(action);
        return isPortAction(action) && action.charAt(action.length() - 1) == '?';
    }

    public static String complement(String action) {
        action = normalize(action);
        if (!isPortAction(action)) {
            return action;
        }
        if (isSend(action)) {
            return channelOf(action) + "?";
        }
        return channelOf(action) + "!";
    }

    public static String stripSuffix(String action) {
        action = normalize(action);
        return isPortAction(action) ? channelOf(action) : action;
    }

    public static String toCq2HookChannel(String baseChannel) {
        baseChannel = normalize(baseChannel);
        if (baseChannel == null || baseChannel.isEmpty()) {
            return baseChannel;
        }
        return CQ2_HOOK_PREFIX + baseChannel;
    }

    public static boolean isCq2HookChannel(String channel) {
        channel = normalize(channel);
        return channel != null && channel.startsWith(CQ2_HOOK_PREFIX);
    }

    public static String unwrapCq2HookChannel(String channel) {
        channel = normalize(channel);
        if (!isCq2HookChannel(channel)) {
            return channel;
        }
        return channel.substring(CQ2_HOOK_PREFIX.length());
    }
}
