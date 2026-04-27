package verification.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class PrimeSplitConfig {
    private final Set<String> channels;

    private PrimeSplitConfig(Set<String> channels) {
        this.channels = Collections.unmodifiableSet(new LinkedHashSet<String>(channels));
    }

    public static PrimeSplitConfig empty() {
        return new PrimeSplitConfig(Collections.<String>emptySet());
    }

    public static PrimeSplitConfig of(String... channels) {
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        if (channels != null) {
            for (String channel : channels) {
                if (channel == null) {
                    continue;
                }
                String trimmed = channel.trim();
                if (!trimmed.isEmpty()) {
                    set.add(trimmed);
                }
            }
        }
        return new PrimeSplitConfig(set);
    }

    public Set<String> getChannels() {
        return channels;
    }

    public boolean isEmpty() {
        return channels.isEmpty();
    }
}
