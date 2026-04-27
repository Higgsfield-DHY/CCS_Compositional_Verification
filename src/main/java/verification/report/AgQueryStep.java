package verification.report;

public class AgQueryStep {
    private int iteration;
    private Boolean cq1Pass;
    private String ctx1;
    private Boolean ctx1InM2;
    private Boolean cq2Pass;
    private String ctx2;
    private Boolean ctx2KeepsPhi;
    private AgDecisionReason decision = AgDecisionReason.NONE;

    public AgQueryStep() {
    }

    public AgQueryStep(int iteration) {
        this.iteration = iteration;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public Boolean getCq1Pass() {
        return cq1Pass;
    }

    public void setCq1Pass(Boolean cq1Pass) {
        this.cq1Pass = cq1Pass;
    }

    public String getCtx1() {
        return ctx1;
    }

    public void setCtx1(String ctx1) {
        this.ctx1 = ctx1;
    }

    public Boolean getCtx1InM2() {
        return ctx1InM2;
    }

    public void setCtx1InM2(Boolean ctx1InM2) {
        this.ctx1InM2 = ctx1InM2;
    }

    public Boolean getCq2Pass() {
        return cq2Pass;
    }

    public void setCq2Pass(Boolean cq2Pass) {
        this.cq2Pass = cq2Pass;
    }

    public String getCtx2() {
        return ctx2;
    }

    public void setCtx2(String ctx2) {
        this.ctx2 = ctx2;
    }

    public Boolean getCtx2KeepsPhi() {
        return ctx2KeepsPhi;
    }

    public void setCtx2KeepsPhi(Boolean ctx2KeepsPhi) {
        this.ctx2KeepsPhi = ctx2KeepsPhi;
    }

    public AgDecisionReason getDecision() {
        return decision;
    }

    public void setDecision(AgDecisionReason decision) {
        this.decision = decision;
    }
}
