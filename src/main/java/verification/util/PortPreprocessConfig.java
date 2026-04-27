package verification.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class PortPreprocessConfig {
    private final PortSplitMode mode;
    private final Set<String> channels;
    private final boolean logicalAliasView;

    private PortPreprocessConfig(PortSplitMode mode, Set<String> channels, boolean logicalAliasView) {
        this.mode = mode == null ? PortSplitMode.PRIME_SPLIT : mode;
        this.channels = Collections.unmodifiableSet(new LinkedHashSet<String>(channels));
        this.logicalAliasView = logicalAliasView;
    }

    public static PortPreprocessConfig empty() {
        return new PortPreprocessConfig(PortSplitMode.PRIME_SPLIT, Collections.<String>emptySet(), false);
    }

    public static PortPreprocessConfig of(PortSplitMode mode, Set<String> channels, boolean logicalAliasView) {
        return new PortPreprocessConfig(mode, normalizeChannels(channels), logicalAliasView);
    }

    public static PortPreprocessConfig primeSplit(Set<String> channels) {
        return new PortPreprocessConfig(PortSplitMode.PRIME_SPLIT, normalizeChannels(channels), false);
    }

    public static PortPreprocessConfig primeSplit(String... channels) {
        return new PortPreprocessConfig(PortSplitMode.PRIME_SPLIT, normalizeChannels(channels), false);
    }

    public static PortPreprocessConfig bidirectionalDomainSplit(boolean logicalAliasView, String... channels) {
        return new PortPreprocessConfig(
                PortSplitMode.BIDIRECTIONAL_DOMAIN_SPLIT,
                normalizeChannels(channels),
                logicalAliasView);
    }

    public PortSplitMode getMode() {
        return mode;
    }

    public Set<String> getChannels() {
        return channels;
    }

    public boolean isLogicalAliasView() {
        return logicalAliasView;
    }

    public boolean isEmpty() {
        return channels.isEmpty();
    }

    private static Set<String> normalizeChannels(Set<String> channels) {
        LinkedHashSet<String> normalized = new LinkedHashSet<String>();
        if (channels == null) {
            return normalized;
        }
        for (String channel : channels) {
            if (channel == null) {
                continue;
            }
            String trimmed = channel.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        return normalized;
    }

    private static Set<String> normalizeChannels(String... channels) {
        LinkedHashSet<String> normalized = new LinkedHashSet<String>();
        if (channels == null) {
            return normalized;
        }
        for (String channel : channels) {
            if (channel == null) {
                continue;
            }
            String trimmed = channel.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        return normalized;
    }
}

