package verification.reset;

import java.util.Set;

public class StaticSigmaResetPolicy implements ResetPolicy {
    private final Set<String> resetActions;

    public StaticSigmaResetPolicy(Set<String> resetActions) {
        this.resetActions = resetActions;
    }

    @Override
    public ResetPolicyType getType() {
        return ResetPolicyType.STATIC_SIGMA;
    }

    @Override
    public ResetDecider newDecider() {
        return (symbol, timeValue) -> resetActions != null && resetActions.contains(symbol);
    }
}
