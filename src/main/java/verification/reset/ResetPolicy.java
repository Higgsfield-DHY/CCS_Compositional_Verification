package verification.reset;

public interface ResetPolicy {
    ResetPolicyType getType();

    ResetDecider newDecider();
}
