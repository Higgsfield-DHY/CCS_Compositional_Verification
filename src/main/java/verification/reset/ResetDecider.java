package verification.reset;

public interface ResetDecider {
    boolean shouldReset(String symbol, Double timeValue);
}
