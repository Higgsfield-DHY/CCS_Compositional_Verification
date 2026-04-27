package verification.frame;

import learn.frame.Learner;
import ta.ota.DOTA;
import ta.ota.ResetLogicTimeWord;
import verification.plugins.MinimalSigma;
import verification.report.AgDecisionReason;
import verification.report.AgRunReport;
import verification.report.AgVerdict;
import verification.teacher.UppaalTeacher;
import verification.util.DisplayRenderUtil;

import java.util.Set;

import static verification.Constant.RESET_SIGMA;

public class CheckFrame {
    private UppaalTeacher teacher;
    private Learner<ResetLogicTimeWord> learner;
    private boolean guessAlphabet = false;
    private MinimalSigma sigmaSelector;

    public CheckFrame(UppaalTeacher teacher, Learner<ResetLogicTimeWord> learner) {
        this.teacher = teacher;
        this.learner = learner;
    }

    public void guessSigmas(Set<String> targetSigma) {
        guessAlphabet = true;
        sigmaSelector = new MinimalSigma(targetSigma);
        teacher.setSigmaSelector(sigmaSelector);
    }

    public void start() {
        long start = System.currentTimeMillis();
        execute();
        long end = System.currentTimeMillis();

        System.out.println("学习结束\n"
                + "总耗时: " + (end - start) + " ms\n"
                + "输入耗时: " + teacher.getDelayTime() + " ms\n"
                + "验证耗时: " + (end - start - teacher.getDelayTime()) + " ms\n"
                + "------------------------------");
    }

    public void start(int repeatCount) {
        long totalTime = 0;
        for (int i = 0; i < repeatCount; i++) {
            long start = System.currentTimeMillis();
            execute();
            long end = System.currentTimeMillis();
            totalTime += end - start;
        }

        System.out.println("学习结束\n"
                + "平均耗时: " + totalTime / repeatCount + " ms\n"
                + "输入平均耗时: " + teacher.getDelayTime() / repeatCount + " ms\n"
                + "验证平均耗时: " + (totalTime / repeatCount - teacher.getDelayTime() / repeatCount) + " ms");
    }

    public AgRunReport startWithReport() {
        long start = System.currentTimeMillis();
        DOTA finalHypothesis = execute();
        long end = System.currentTimeMillis();

        AgRunReport report = teacher.getLastRunReport();
        if (report == null) {
            report = new AgRunReport();
        }
        report.setElapsedMs(end - start);
        if (finalHypothesis != null) {
            report.setFinalHypothesis(finalHypothesis.toString());
            report.setFinalStateCount(finalHypothesis.getLocations().size());
        }
        if (report.getVerdict() == AgVerdict.UNKNOWN) {
            report.setVerdict(AgVerdict.SAFE);
            if (report.getDecisionReason() == AgDecisionReason.NONE) {
                report.setDecisionReason(AgDecisionReason.SAFE_ALL_QUERIES_PASS);
            }
        }
        return report;
    }

    private DOTA execute() {
        long begin = System.currentTimeMillis();
        if (guessAlphabet) {
            sigmaSelector.init();
            learner.init(sigmaSelector.getNext());
        } else {
            learner.init();
        }
        learner.learn();
        System.out.println("学习初始化耗时 " + (System.currentTimeMillis() - begin));
        begin = System.currentTimeMillis();

        learner.show();
        DOTA hypothesis = learner.buildHypothesis();
        System.out.println(DisplayRenderUtil.renderHypothesis(hypothesis));
        System.out.println("构建假设自动机耗时 " + (System.currentTimeMillis() - begin));

        ResetLogicTimeWord ce;
        while (null != (ce = teacher.equivalence(hypothesis))) {
            if (RESET_SIGMA.equals(ce)) {
                Set<String> curSigma = sigmaSelector.getCur();
                System.out.println("重构字母表为 " + curSigma);
                learner.init(curSigma);
                learner.learn();
                learner.show();
            } else {
                System.out.println("反例是：" + DisplayRenderUtil.renderResetWord(ce));
                learner.refine(ce);
                learner.show();
            }

            hypothesis = learner.buildHypothesis();
            System.out.println(DisplayRenderUtil.renderHypothesis(hypothesis));
        }
        return hypothesis;
    }

    public UppaalTeacher getTeacher() {
        return teacher;
    }

    public Learner<ResetLogicTimeWord> getLearner() {
        return learner;
    }
}