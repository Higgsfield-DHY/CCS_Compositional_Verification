package verification.frame;

import learn.frame.EquivalenceQuery;
import ta.TimeGuard;
import ta.dbm.ActionGuard;
import ta.ota.DOTA;
import ta.ota.LogicTimeWord;
import ta.ota.LogicTimedAction;
import ta.ota.ResetLogicAction;
import ta.ota.ResetLogicTimeWord;
import verification.Config;
import verification.frame.checkerimpl.Promise1Checker;
import verification.frame.checkerimpl.Promise2Checker;
import verification.plugins.MinimalSigma;
import verification.report.AgDecisionReason;
import verification.report.AgQueryStep;
import verification.report.AgRunReport;
import verification.report.AgVerdict;
import verification.reset.ResetDecider;
import verification.reset.ResetPolicy;
import verification.uppaal.verify.Result;
import verification.util.PortActionUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static verification.Constant.CHECK_FAILED;
import static verification.Constant.RESET_SIGMA;

public class UppaalEquivalenceQuery implements EquivalenceQuery<ResetLogicTimeWord, DOTA> {
    private final Promise1Checker promise1Checker;
    private final Promise2Checker promise2Checker;
    private final Set<String> targetSigmaSet;
    private final boolean portActionMode;
    private final ResetPolicy resetPolicy;

    private List<ResetLogicTimeWord> promise1CounterExamples;
    private List<ResetLogicTimeWord> promise2CounterExamples;
    private MinimalSigma sigmaSelector;
    private long delay;
    private int count;

    private AgRunReport runReport;
    private AgQueryStep currentStep;

    public UppaalEquivalenceQuery(Promise1Checker promise1Checker, Promise2Checker promise2Checker,
                                  Set<String> targetSigmaSet,
                                  boolean portActionMode, ResetPolicy resetPolicy) {
        this.promise1Checker = promise1Checker;
        this.promise2Checker = promise2Checker;
        this.targetSigmaSet = targetSigmaSet;
        this.portActionMode = portActionMode;
        this.resetPolicy = resetPolicy;
        initCERecorder();
        resetRunReport("AG");
    }

    @Override
    public ResetLogicTimeWord findCounterExample(DOTA hypothesis) {
        count++;
        currentStep = new AgQueryStep(count);

        long begin = System.currentTimeMillis();
        ResetLogicTimeWord ctx = equivalenceQuery1(hypothesis);
        System.out.println("eq1:耗时 " + (System.currentTimeMillis() - begin));
        if (ctx != null) {
            appendCurrentStep();
            return ctx;
        }

        begin = System.currentTimeMillis();
        ctx = equivalenceQuery2(hypothesis);
        System.out.println("eq2:耗时 " + (System.currentTimeMillis() - begin));
        if (ctx == null && runReport.getVerdict() == AgVerdict.UNKNOWN) {
            currentStep.setDecision(AgDecisionReason.SAFE_ALL_QUERIES_PASS);
            runReport.setVerdict(AgVerdict.SAFE);
            runReport.setDecisionReason(AgDecisionReason.SAFE_ALL_QUERIES_PASS);
        }
        appendCurrentStep();
        return ctx;
    }

    private ResetLogicTimeWord equivalenceQuery1(DOTA hypothesis) {
        Result checkResult = promise1Checker.isSatisfied(hypothesis, hypothesis.getSigma());
        boolean cq1Pass = checkResult.isSatisfy();
        currentStep.setCq1Pass(cq1Pass);
        if (cq1Pass) {
            return null;
        }

        if (hypothesis.getAcceptedLocations().isEmpty() && sigmaSelector != null) {
            sigmaSelector.getNext();
            initCERecorder();
            currentStep.setDecision(AgDecisionReason.RESET_SIGMA);
            return RESET_SIGMA;
        }

        ResetLogicTimeWord ctx = logicTimeWord2ResetLogicTimeWord(
                checkResult.getLogicTimeWord(), false);
        currentStep.setCtx1(String.valueOf(ctx));

        boolean ctx1InTargetM2 = promise2Checker.isSatisfied(ctx, targetSigmaSet).isSatisfy();
        currentStep.setCtx1InM2(ctx1InTargetM2);
        if (Config.COMPLETED_EXAMPLE && ctx1InTargetM2) {
            markUnsafe(AgDecisionReason.CQ1_CTX1_IN_M2);
            System.out.println(promise1Checker.getStatement() + " 性质不满足，反例是 " + ctx);
            return CHECK_FAILED;
        }

        if (hypothesis.getSigma().containsAll(targetSigmaSet)) {
            if (!Config.COMPLETED_EXAMPLE && ctx1InTargetM2) {
                markUnsafe(AgDecisionReason.CQ1_CTX1_IN_M2);
                System.out.println(promise1Checker.getStatement() + " 性质不满足，反例是 " + ctx);
                return CHECK_FAILED;
            }
            currentStep.setDecision(AgDecisionReason.CQ1_COUNTEREXAMPLE);
            return ctx;
        }

        promise1CounterExamples.add(ctx);
        if (promise2Checker.isSatisfied(ctx, hypothesis.getSigma()).isSatisfy()) {
            findNextSigma();
            initCERecorder();
            currentStep.setDecision(AgDecisionReason.RESET_SIGMA);
            return RESET_SIGMA;
        }

        currentStep.setDecision(AgDecisionReason.CQ1_COUNTEREXAMPLE);
        return ctx;
    }

    private ResetLogicTimeWord equivalenceQuery2(DOTA hypothesis) {
        Result verifyResult = promise2Checker.isSatisfied(hypothesis, hypothesis.getSigma());
        boolean cq2Pass = verifyResult.isSatisfy();
        currentStep.setCq2Pass(cq2Pass);
        if (cq2Pass) {
            System.out.println(promise1Checker.getStatement() + " 性质满足");
            return null;
        }

        ResetLogicTimeWord ctx = logicTimeWord2ResetLogicTimeWord(
                verifyResult.getLogicTimeWord(), true);
        currentStep.setCtx2(String.valueOf(ctx));

        boolean ctx2KeepsTargetPhi = promise1Checker.isSatisfied(ctx, targetSigmaSet).isSatisfy();
        currentStep.setCtx2KeepsPhi(ctx2KeepsTargetPhi);
        if (Config.COMPLETED_EXAMPLE && !ctx2KeepsTargetPhi) {
            markUnsafe(AgDecisionReason.CQ2_CTX2_BREAKS_PHI);
            System.out.println(promise1Checker.getStatement() + " 性质不满足，反例是 " + ctx);
            return CHECK_FAILED;
        }

        if (hypothesis.getSigma().containsAll(targetSigmaSet)) {
            if (!Config.COMPLETED_EXAMPLE && !ctx2KeepsTargetPhi) {
                markUnsafe(AgDecisionReason.CQ2_CTX2_BREAKS_PHI);
                System.out.println(promise1Checker.getStatement() + " 性质不满足，反例是 " + ctx);
                return CHECK_FAILED;
            }
            currentStep.setDecision(AgDecisionReason.CQ2_COUNTEREXAMPLE);
            return ctx;
        }

        promise2CounterExamples.add(ctx);
        if (!promise1Checker.isSatisfied(ctx, hypothesis.getSigma()).isSatisfy()) {
            findNextSigma();
            initCERecorder();
            currentStep.setDecision(AgDecisionReason.RESET_SIGMA);
            return RESET_SIGMA;
        }

        currentStep.setDecision(AgDecisionReason.CQ2_COUNTEREXAMPLE);
        return ctx;
    }

    private void appendCurrentStep() {
        if (currentStep == null) {
            return;
        }
        runReport.appendStep(currentStep);
        currentStep = null;
    }

    private void markUnsafe(AgDecisionReason reason) {
        currentStep.setDecision(reason);
        runReport.setVerdict(AgVerdict.UNSAFE);
        runReport.setDecisionReason(reason);
    }

    public void resetRunReport(String caseName) {
        runReport = new AgRunReport();
        runReport.setCaseName(caseName);
        runReport.setResetPolicyType(resetPolicy.getType().name());
    }

    public AgRunReport getRunReport() {
        return runReport;
    }

    private void initCERecorder() {
        promise1CounterExamples = new ArrayList<>();
        promise2CounterExamples = new ArrayList<>();
    }

    private boolean findNextSigma() {
        if (sigmaSelector == null) {
            return false;
        }
        while (sigmaSelector.hasNext()) {
            Set<String> nextSigma = sigmaSelector.getNext();
            if (!Config.COMPLETED_EXAMPLE || checkSigma(nextSigma)) {
                return true;
            }
        }
        System.out.println("未发现符合要求的字母表，请检查代码");
        return false;
    }

    private boolean checkSigma(Set<String> nextSigma) {
        for (ResetLogicTimeWord e1 : promise1CounterExamples) {
            if (promise2Checker.isSatisfied(e1, nextSigma).isSatisfy()) {
                return false;
            }
        }
        for (ResetLogicTimeWord e2 : promise2CounterExamples) {
            if (!promise1Checker.isSatisfied(e2, nextSigma).isSatisfy()) {
                return false;
            }
        }
        return true;
    }

    private ResetLogicTimeWord analyseCtx(List<ActionGuard> actionGuards) {
        ResetLogicTimeWord resetLogicTimeWord = ResetLogicTimeWord.emptyWord();
        ResetDecider decider = resetPolicy.newDecider();
        for (ActionGuard actionGuard : actionGuards) {
            String symbol = actionGuard.getSymbol();
            Double value = actionGuard.getLowerValue();
            boolean reset = decider.shouldReset(symbol, value);
            ResetLogicAction logicAction = new ResetLogicAction(symbol, value, reset);
            resetLogicTimeWord = resetLogicTimeWord.concat(logicAction);
        }
        return resetLogicTimeWord;
    }

    private ResetLogicTimeWord logicTimeWord2ResetLogicTimeWord(LogicTimeWord timeWord,
                                                                 boolean reverseToLearnerAlphabet) {
        ResetLogicTimeWord resetLogicTimeWord = ResetLogicTimeWord.emptyWord();
        ResetDecider decider = resetPolicy.newDecider();
        for (LogicTimedAction timedAction : timeWord.getTimedActions()) {
            String symbol = mapSymbolToLearnerAlphabet(timedAction.getSymbol(), reverseToLearnerAlphabet);
            if (symbol == null) {
                continue;
            }
            double value = timedAction.getValue();
            if (portActionMode) {
                if (Math.abs(value - Math.rint(value)) >= 1e-9) {
                    value = Math.floor(value) + 0.1d;
                } else {
                    value = Math.rint(value);
                }
            }
            ResetLogicAction logicAction = new ResetLogicAction(symbol, value, decider.shouldReset(symbol, value));
            resetLogicTimeWord = resetLogicTimeWord.concat(logicAction);
        }
        return resetLogicTimeWord;
    }

    private String mapSymbolToLearnerAlphabet(String rawSymbol, boolean reverseToLearnerAlphabet) {
        String symbol = PortActionUtil.normalize(rawSymbol);
        if (symbol == null || symbol.isEmpty()) {
            return null;
        }
        if (!portActionMode) {
            return PortActionUtil.stripSuffix(symbol);
        }
        if (!PortActionUtil.isPortAction(symbol)) {
            return null;
        }
        String channel = PortActionUtil.channelOf(symbol);
        if (PortActionUtil.isCq2HookChannel(channel)) {
            char suffix = symbol.charAt(symbol.length() - 1);
            symbol = PortActionUtil.unwrapCq2HookChannel(channel) + suffix;
        }
        String mapped = reverseToLearnerAlphabet ? PortActionUtil.complement(symbol) : symbol;
        if (!targetSigmaSet.contains(mapped)) {
            return null;
        }
        return mapped;
    }

    private List<ActionGuard> readCounterTrace() throws IOException {
        System.out.println("请给出一个 counterTrace：先给长度 n，再给出 2n 行输入");
        BufferedReader rd = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(rd.readLine());
        List<ActionGuard> actionGuards = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String symbol = rd.readLine().trim();
            String pattern = rd.readLine().trim();
            TimeGuard guard = new TimeGuard(pattern);
            ActionGuard node = new ActionGuard(symbol, guard);
            actionGuards.add(node);
        }
        return actionGuards;
    }

    @Override
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public MinimalSigma getSigmaSelector() {
        return sigmaSelector;
    }

    public void setSigmaSelector(MinimalSigma sigmaSelector) {
        this.sigmaSelector = sigmaSelector;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public boolean isPortActionMode() {
        return portActionMode;
    }
}
