package verification.report;

public enum AgDecisionReason {
    NONE,
    SAFE_ALL_QUERIES_PASS,
    CQ1_COUNTEREXAMPLE,
    CQ2_COUNTEREXAMPLE,
    CQ1_CTX1_IN_M2,
    CQ2_CTX2_BREAKS_PHI,
    RESET_SIGMA
}
