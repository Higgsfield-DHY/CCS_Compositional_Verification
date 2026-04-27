package verification.report;

import java.util.ArrayList;
import java.util.List;

public class AgRunReport {
    private String caseName;
    private String resetPolicyType;
    private String directTruth;
    private AgVerdict verdict = AgVerdict.UNKNOWN;
    private AgDecisionReason decisionReason = AgDecisionReason.NONE;
    private List<AgQueryStep> steps = new ArrayList<>();
    private int cq1FailCount;
    private int cq2FailCount;
    private String finalHypothesis;
    private int finalStateCount;
    private long elapsedMs;

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getResetPolicyType() {
        return resetPolicyType;
    }

    public void setResetPolicyType(String resetPolicyType) {
        this.resetPolicyType = resetPolicyType;
    }

    public String getDirectTruth() {
        return directTruth;
    }

    public void setDirectTruth(String directTruth) {
        this.directTruth = directTruth;
    }

    public AgVerdict getVerdict() {
        return verdict;
    }

    public void setVerdict(AgVerdict verdict) {
        this.verdict = verdict;
    }

    public AgDecisionReason getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(AgDecisionReason decisionReason) {
        this.decisionReason = decisionReason;
    }

    public List<AgQueryStep> getSteps() {
        return steps;
    }

    public void setSteps(List<AgQueryStep> steps) {
        this.steps = steps;
    }

    public int getCq1FailCount() {
        return cq1FailCount;
    }

    public void setCq1FailCount(int cq1FailCount) {
        this.cq1FailCount = cq1FailCount;
    }

    public int getCq2FailCount() {
        return cq2FailCount;
    }

    public void setCq2FailCount(int cq2FailCount) {
        this.cq2FailCount = cq2FailCount;
    }

    public String getFinalHypothesis() {
        return finalHypothesis;
    }

    public void setFinalHypothesis(String finalHypothesis) {
        this.finalHypothesis = finalHypothesis;
    }

    public int getFinalStateCount() {
        return finalStateCount;
    }

    public void setFinalStateCount(int finalStateCount) {
        this.finalStateCount = finalStateCount;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }

    public void appendStep(AgQueryStep step) {
        if (step == null) {
            return;
        }
        steps.add(step);
        if (Boolean.FALSE.equals(step.getCq1Pass())) {
            cq1FailCount++;
        }
        if (Boolean.FALSE.equals(step.getCq2Pass())) {
            cq2FailCount++;
        }
    }
}
