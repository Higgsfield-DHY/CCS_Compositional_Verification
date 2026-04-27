package verification.util;

import java.util.Collections;
import java.util.Map;

/**
 * Holds per-thread action alias mapping for log rendering only.
 */
public final class DisplayAliasContext {
    private static final ThreadLocal<ChannelAliasRegistry> ALIAS_REGISTRY =
            new ThreadLocal<ChannelAliasRegistry>();

    private DisplayAliasContext() {
    }

    public static void set(ChannelAliasRegistry aliasRegistry) {
        if (aliasRegistry == null) {
            ALIAS_REGISTRY.remove();
            return;
        }
        ALIAS_REGISTRY.set(aliasRegistry);
    }

    public static void clear() {
        ALIAS_REGISTRY.remove();
    }

    public static String aliasAction(String action) {
        String normalized = PortActionUtil.normalize(action);
        if (normalized == null || normalized.isEmpty()) {
            return action;
        }
        ChannelAliasRegistry aliasRegistry = ALIAS_REGISTRY.get();
        if (aliasRegistry == null || aliasRegistry.isEmpty()) {
            return normalized;
        }
        String alias = aliasRegistry.view().get(normalized);
        return alias == null ? normalized : alias;
    }

    public static Map<String, String> aliasMap() {
        ChannelAliasRegistry aliasRegistry = ALIAS_REGISTRY.get();
        if (aliasRegistry == null || aliasRegistry.isEmpty()) {
            return Collections.emptyMap();
        }
        return aliasRegistry.view();
    }
}

