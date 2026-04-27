package verification.reset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DynamicGammaResetPolicy implements ResetPolicy {
    private final double epsilon;
    private final Set<String> seedActions;

    public DynamicGammaResetPolicy(ResetHeuristicConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("ResetHeuristicConfig must not be null.");
        }
        this.epsilon = config.getEpsilon();
        this.seedActions = new HashSet<>(config.getSeedActions());
    }

    @Override
    public ResetPolicyType getType() {
        return ResetPolicyType.DYNAMIC_GAMMA;
    }

    @Override
    public ResetDecider newDecider() {
        return new GammaDecider(seedActions, epsilon);
    }

    private static class GammaDecider implements ResetDecider {
        private final Set<String> seedActions;
        private final double epsilon;
        private final Map<String, Double> channelLastTime = new HashMap<>();
        private Double lastTime;
        private int index;

        private GammaDecider(Set<String> seedActions, double epsilon) {
            this.seedActions = seedActions;
            this.epsilon = epsilon;
        }

        @Override
        public boolean shouldReset(String symbol, Double timeValue) {
            index++;
            boolean reset = seedActions.contains(symbol);
            if (timeValue != null) {
                if (!reset && index > 1 && lastTime != null && timeValue + epsilon < lastTime) {
                    reset = true;
                }
                Double sameChannelLast = channelLastTime.get(symbol);
                if (!reset && sameChannelLast != null && timeValue + epsilon < sameChannelLast) {
                    reset = true;
                }
                lastTime = timeValue;
                if (symbol != null) {
                    channelLastTime.put(symbol, timeValue);
                }
            }
            return reset;
        }
    }
}
