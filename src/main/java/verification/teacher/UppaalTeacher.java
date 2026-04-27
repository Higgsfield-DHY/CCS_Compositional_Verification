package verification.teacher;

import learn.defaultteacher.BooleanAnswer;
import learn.frame.Teacher;
import ta.ota.DOTA;
import ta.ota.LogicTimeWord;
import ta.ota.ResetLogicAction;
import ta.ota.ResetLogicTimeWord;
import verification.frame.UppaalEquivalenceQuery;
import verification.frame.Cq2Mode;
import verification.frame.UppaalMembership;
import verification.frame.checkerimpl.Promise1Checker;
import verification.frame.checkerimpl.Promise2Checker;
import verification.plugins.MinimalSigma;
import verification.plugins.SequenceChecker;
import verification.report.AgRunReport;
import verification.reset.ResetDecider;
import verification.reset.ResetHeuristicConfig;
import verification.reset.ResetPolicy;
import verification.reset.ResetPolicyFactory;
import verification.reset.ResetPolicyType;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.util.PortActionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static verification.Constant.CHECK_FAILED;
import static verification.Constant.RESET_SIGMA;

public class UppaalTeacher implements Teacher<ResetLogicTimeWord, ResetLogicTimeWord, BooleanAnswer, DOTA, LogicTimeWord> {

    private final UppaalMembership membership;
    private final UppaalEquivalenceQuery equivalenceQuery;
    private final Set<String> targetSigma;
    private final ResetPolicy resetPolicy;
    private MinimalSigma sigmaSelector;

    public UppaalTeacher(List<Template> m1, List<Template> m2, String statement, Declaration globalDeclaration,
                         Map<String, Boolean> syncSendMap, Set<String> resetSigma, Set<String> targetSigma,
                         boolean portActionMode, ResetPolicyType resetPolicyType,
                         ResetHeuristicConfig resetHeuristicConfig, Cq2Mode cq2Mode) {
        this.targetSigma = targetSigma;
        this.resetPolicy = ResetPolicyFactory.create(resetPolicyType, resetSigma, resetHeuristicConfig);
        Promise1Checker promise1Checker = new Promise1Checker(globalDeclaration, m1, statement,
                syncSendMap, targetSigma, portActionMode);
        Promise2Checker promise2Checker = new Promise2Checker(m2, syncSendMap, globalDeclaration,
                targetSigma, portActionMode, cq2Mode);

        membership = new UppaalMembership(promise1Checker);
        equivalenceQuery = new UppaalEquivalenceQuery(promise1Checker, promise2Checker,
                targetSigma, portActionMode, resetPolicy);
    }

    public long getDelayTime() {
        return equivalenceQuery.getDelay();
    }

    public void beginRunReport(String caseName) {
        equivalenceQuery.resetRunReport(caseName);
    }

    public AgRunReport getLastRunReport() {
        return equivalenceQuery.getRunReport();
    }

    public ResetPolicyType getResetPolicyType() {
        return resetPolicy.getType();
    }

    public void setSequencePlugin(List<SequenceChecker> plugins) {
        this.membership.setPlugins(plugins);
    }

    @Override
    public BooleanAnswer membership(ResetLogicTimeWord timedWord) {
        if (sigmaSelector == null) {
            return membership.answer(timedWord, targetSigma);
        }
        return membership.answer(timedWord, sigmaSelector.getCur());
    }

    @Override
    public ResetLogicTimeWord equivalence(DOTA hypothesis) {
        long begin = System.currentTimeMillis();
        ResetLogicTimeWord answer = equivalenceQuery.findCounterExample(hypothesis);
        if (RESET_SIGMA.equals(answer)) {
            membership.initCache();
            return answer;
        }
        if (CHECK_FAILED.equals(answer)) {
            // Unsafe verdict is recorded inside the equivalence-query report.
            return null;
        }
        if (answer != null && sigmaSelector != null) {
            List<ResetLogicAction> resetLogicActions = answer.getTimedActions().stream()
                    .filter(action -> sigmaSelector.getCur().contains(action.getSymbol()))
                    .collect(Collectors.toList());
            answer = new ResetLogicTimeWord(resetLogicActions);
        }

        System.out.println("等价查询耗时 " + (System.currentTimeMillis() - begin));
        return answer;
    }

    @Override
    public ResetLogicTimeWord transferWord(LogicTimeWord timeWord) {
        List<ResetLogicAction> resetLogicActions = new ArrayList<>();
        boolean portActionMode = equivalenceQuery.isPortActionMode();
        ResetDecider decider = resetPolicy.newDecider();
        timeWord.getTimedActions().forEach(e -> {
            String symbol = PortActionUtil.normalize(e.getSymbol());
            if (!portActionMode) {
                symbol = PortActionUtil.stripSuffix(symbol);
            }
            Double value = normalizeTime(e.getValue(), portActionMode);
            boolean reset = decider.shouldReset(symbol, value);
            ResetLogicAction resetLogicAction = new ResetLogicAction(symbol, value, reset);
            resetLogicActions.add(resetLogicAction);
        });
        return new ResetLogicTimeWord(resetLogicActions);
    }

    private static Double normalizeTime(Double value, boolean portActionMode) {
        if (!portActionMode || value == null) {
            return value;
        }
        double v = value;
        if (Math.abs(v - Math.rint(v)) < 1e-9) {
            return Math.rint(v);
        }
        return Math.floor(v) + 0.1d;
    }

    public void setSigmaSelector(MinimalSigma sigmaSelector) {
        this.sigmaSelector = sigmaSelector;
        equivalenceQuery.setSigmaSelector(sigmaSelector);
    }
}
