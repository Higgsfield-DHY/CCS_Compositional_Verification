package verification.reset;

import java.util.HashSet;
import java.util.Set;

public final class ResetPolicyFactory {
    private ResetPolicyFactory() {
    }

    public static ResetPolicy create(ResetPolicyType type, Set<String> resetSigma, ResetHeuristicConfig config) {
        ResetPolicyType actualType = type == null ? ResetPolicyType.STATIC_SIGMA : type;
        if (actualType == ResetPolicyType.DYNAMIC_GAMMA) {
            ResetHeuristicConfig actualConfig = config == null
                    ? ResetHeuristicConfig.fromSeedActions(resetSigma)
                    : config;
            return new DynamicGammaResetPolicy(actualConfig);
        }
        Set<String> sigma = resetSigma == null ? new HashSet<>() : new HashSet<>(resetSigma);
        return new StaticSigmaResetPolicy(sigma);
    }
}
