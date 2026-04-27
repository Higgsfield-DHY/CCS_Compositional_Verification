package verification.experiment.channel;

import verification.util.PortActionUtil;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ChannelPortMapper {
    private ChannelPortMapper() {
    }

    public static Set<String> inferTargetSigmaFromMap(Map<String, Boolean> syncSendMap) {
        if (syncSendMap == null || syncSendMap.isEmpty()) {
            throw new IllegalArgumentException("syncSendMap must not be empty.");
        }
        Set<String> sigma = new LinkedHashSet<>();
        for (Map.Entry<String, Boolean> entry : syncSendMap.entrySet()) {
            String channel = PortActionUtil.normalize(entry.getKey());
            if (channel == null || channel.isEmpty()) {
                continue;
            }
            if (PortActionUtil.isPortAction(channel)) {
                sigma.add(channel);
                continue;
            }
            sigma.add(channel + (Boolean.TRUE.equals(entry.getValue()) ? "!" : "?"));
        }
        if (sigma.isEmpty()) {
            throw new IllegalStateException("Failed to infer target sigma from syncSendMap.");
        }
        return sigma;
    }

    public static Set<String> mapResetSeedToPort(Set<String> resetSeed, Map<String, Boolean> syncSendMap) {
        Set<String> mapped = new LinkedHashSet<>();
        if (resetSeed == null || resetSeed.isEmpty()) {
            return mapped;
        }
        for (String seed : resetSeed) {
            String symbol = PortActionUtil.normalize(seed);
            if (symbol == null || symbol.isEmpty()) {
                continue;
            }
            if (PortActionUtil.isPortAction(symbol)) {
                mapped.add(symbol);
                continue;
            }
            Boolean direction = syncSendMap.get(symbol);
            if (direction == null) {
                throw new IllegalStateException("Reset seed action not found in syncSendMap: " + symbol);
            }
            mapped.add(symbol + (direction ? "!" : "?"));
        }
        return mapped;
    }
}
