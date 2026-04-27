package verification.reset;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ResetHeuristicConfig {
    private final double epsilon;
    private final Set<String> seedActions;

    public ResetHeuristicConfig(double epsilon, Set<String> seedActions) {
        this.epsilon = epsilon;
        Set<String> copy = seedActions == null ? new HashSet<>() : new HashSet<>(seedActions);
        this.seedActions = Collections.unmodifiableSet(copy);
    }

    public double getEpsilon() {
        return epsilon;
    }

    public Set<String> getSeedActions() {
        return seedActions;
    }

    public static ResetHeuristicConfig fromSeedActions(Set<String> seedActions) {
        return new ResetHeuristicConfig(1e-9, seedActions);
    }
}
