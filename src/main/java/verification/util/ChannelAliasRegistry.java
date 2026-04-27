package verification.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ChannelAliasRegistry {
    private final Map<String, String> physicalToLogical = new LinkedHashMap<String, String>();

    public void registerActionAlias(String physicalAction, String logicalAction) {
        if (physicalAction == null || logicalAction == null) {
            return;
        }
        String physical = PortActionUtil.normalize(physicalAction);
        String logical = PortActionUtil.normalize(logicalAction);
        if (physical == null || logical == null || physical.isEmpty() || logical.isEmpty()) {
            return;
        }
        physicalToLogical.put(physical, logical);
    }

    public Map<String, String> view() {
        return Collections.unmodifiableMap(physicalToLogical);
    }

    public boolean isEmpty() {
        return physicalToLogical.isEmpty();
    }
}

