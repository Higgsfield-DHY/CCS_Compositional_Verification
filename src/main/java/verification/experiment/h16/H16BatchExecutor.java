package verification.experiment.h16;

import ta.ota.DOTA;
import ta.ota.ResetLogicTimeWord;
import verification.report.AgQueryStep;
import verification.report.AgRunReport;
import verification.report.AgVerdict;
import verification.teacher.UppaalTeacher;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.util.ChannelPreprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class H16BatchExecutor {
    public static void main(String[] args) throws IOException {
        List<BatchRow> rows = new ArrayList<>();

        for (H16ScenarioConfig config : H16Scenarios.learningSuite()) {
            rows.add(runLearningCase(config));
        }
        rows.add(runProbeCq1Secondary());
        rows.add(runProbeCq2Secondary());

        printRows(rows);

        boolean allPass = true;
        for (BatchRow row : rows) {
            if (!row.pass) {
                allPass = false;
                break;
            }
        }
        if (!allPass) {
            System.err.println("H1-6 batch validation failed.");
            System.exit(1);
        }
    }

    private static BatchRow runLearningCase(H16ScenarioConfig config) throws IOException {
        H16ScenarioExperiment experiment = new H16ScenarioExperiment(config);
        AgRunReport report = experiment.executeWithReport(true, false, false, 1);

        boolean verdictOk = report.getVerdict() == config.getExpectedVerdict();
        boolean cq1Ok = report.getCq1FailCount() >= config.getMinCq1Fails();
        boolean cq2Ok = report.getCq2FailCount() >= config.getMinCq2Fails();
        boolean pass = verdictOk && cq1Ok && cq2Ok;

        return new BatchRow(
                config.getCaseName(),
                config.getExpectedVerdict().name(),
                report.getVerdict().name(),
                report.getCq1FailCount(),
                report.getCq2FailCount(),
                summarizeSecondary(report),
                report.getFinalStateCount(),
                report.getElapsedMs(),
                pass
        );
    }

    private static BatchRow runProbeCq1Secondary() throws IOException {
        H16ScenarioExperiment experiment = new H16ScenarioExperiment(H16Scenarios.c5UnsafeBThenA());
        UppaalTeacher teacher = buildTeacher(experiment);
        DOTA candidate = H16CandidateUtil.buildA0(experiment.getTargetSigma());

        long begin = System.currentTimeMillis();
        teacher.beginRunReport("C7_PROBE_CQ1_SECONDARY");
        teacher.equivalence(candidate);
        long end = System.currentTimeMillis();

        AgRunReport report = teacher.getLastRunReport();
        report.setElapsedMs(end - begin);
        report.setFinalStateCount(candidate.getLocations().size());
        report.setFinalHypothesis(candidate.toString());
        boolean hasExpectedBranch = hasCq1FailWithCtx1InM2True(report);
        boolean pass = hasExpectedBranch && report.getVerdict() == AgVerdict.UNSAFE;
        String actual = hasExpectedBranch
                ? "CQ1 fail + ctx1InM2=true"
                : "CQ1 secondary branch not observed";

        return new BatchRow(
                "C7_PROBE_CQ1_SECONDARY",
                "CQ1 fail + ctx1InM2=true",
                actual,
                report.getCq1FailCount(),
                report.getCq2FailCount(),
                summarizeSecondary(report),
                report.getFinalStateCount(),
                report.getElapsedMs(),
                pass
        );
    }

    private static BatchRow runProbeCq2Secondary() throws IOException {
        H16ScenarioExperiment experiment = new H16ScenarioExperiment(H16Scenarios.c1BaseSafe());
        UppaalTeacher teacher = buildTeacher(experiment);
        DOTA candidate = H16CandidateUtil.buildATight(experiment.getTargetSigma());

        long begin = System.currentTimeMillis();
        teacher.beginRunReport("C8_PROBE_CQ2_SECONDARY");
        teacher.equivalence(candidate);
        long end = System.currentTimeMillis();

        AgRunReport report = teacher.getLastRunReport();
        report.setElapsedMs(end - begin);
        report.setFinalStateCount(candidate.getLocations().size());
        report.setFinalHypothesis(candidate.toString());
        boolean hasExpectedBranch = hasCq2FailWithCtx2KeepsPhiTrue(report);
        boolean pass = hasExpectedBranch && report.getVerdict() != AgVerdict.UNSAFE;
        String actual = hasExpectedBranch
                ? "CQ2 fail + ctx2KeepsPhi=true"
                : "CQ2 secondary branch not observed";

        return new BatchRow(
                "C8_PROBE_CQ2_SECONDARY",
                "CQ2 fail + ctx2KeepsPhi=true",
                actual,
                report.getCq1FailCount(),
                report.getCq2FailCount(),
                summarizeSecondary(report),
                report.getFinalStateCount(),
                report.getElapsedMs(),
                pass
        );
    }

    private static UppaalTeacher buildTeacher(H16ScenarioExperiment experiment) throws IOException {
        Declaration globalDeclaration = experiment.getGlobalDeclaration();
        List<Template> m1 = experiment.getM1();
        List<Template> m2 = experiment.getM2();
        Set<String> targetSigma = experiment.getTargetSigma();
        if (experiment.isPortActionMode()) {
            ChannelPreprocessor.preprocessPortMode(
                    globalDeclaration, m1, m2, experiment.getM1RenameMap(), experiment.getM2RenameMap(),
                    targetSigma, experiment.getPortPreprocessConfig());
        }
        return new UppaalTeacher(
                m1, m2, experiment.getStatement(), globalDeclaration,
                experiment.getSyncSendMap(), experiment.getResetSigma(), targetSigma, experiment.isPortActionMode(),
                experiment.getResetPolicyType(), experiment.getResetHeuristicConfig(), experiment.getCq2Mode());
    }

    private static boolean hasCq1FailWithCtx1InM2True(AgRunReport report) {
        for (AgQueryStep step : report.getSteps()) {
            if (Boolean.FALSE.equals(step.getCq1Pass()) && Boolean.TRUE.equals(step.getCtx1InM2())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasCq2FailWithCtx2KeepsPhiTrue(AgRunReport report) {
        for (AgQueryStep step : report.getSteps()) {
            if (Boolean.FALSE.equals(step.getCq2Pass()) && Boolean.TRUE.equals(step.getCtx2KeepsPhi())) {
                return true;
            }
        }
        return false;
    }

    private static String summarizeSecondary(AgRunReport report) {
        int ctx1T = 0;
        int ctx1F = 0;
        int ctx2T = 0;
        int ctx2F = 0;
        for (AgQueryStep step : report.getSteps()) {
            if (step.getCtx1InM2() != null) {
                if (step.getCtx1InM2()) {
                    ctx1T++;
                } else {
                    ctx1F++;
                }
            }
            if (step.getCtx2KeepsPhi() != null) {
                if (step.getCtx2KeepsPhi()) {
                    ctx2T++;
                } else {
                    ctx2F++;
                }
            }
        }
        return "ctx1InM2(T/F)=" + ctx1T + "/" + ctx1F + ", ctx2KeepsPhi(T/F)=" + ctx2T + "/" + ctx2F;
    }

    private static void printRows(List<BatchRow> rows) {
        System.out.println("| Case | Expected | Actual | CQ1 fails | CQ2 fails | Secondary checks | Final A states | Time(ms) |");
        System.out.println("| --- | --- | --- | --- | --- | --- | --- | --- |");
        for (BatchRow row : rows) {
            System.out.println("| " + row.caseName
                    + " | " + row.expected
                    + " | " + row.actual
                    + " | " + row.cq1Fails
                    + " | " + row.cq2Fails
                    + " | " + row.secondary
                    + " | " + row.finalStates
                    + " | " + row.timeMs
                    + " |");
        }
    }

    private static class BatchRow {
        private final String caseName;
        private final String expected;
        private final String actual;
        private final int cq1Fails;
        private final int cq2Fails;
        private final String secondary;
        private final int finalStates;
        private final long timeMs;
        private final boolean pass;

        private BatchRow(String caseName, String expected, String actual, int cq1Fails, int cq2Fails,
                         String secondary, int finalStates, long timeMs, boolean pass) {
            this.caseName = caseName;
            this.expected = expected;
            this.actual = actual;
            this.cq1Fails = cq1Fails;
            this.cq2Fails = cq2Fails;
            this.secondary = secondary;
            this.finalStates = finalStates;
            this.timeMs = timeMs;
            this.pass = pass;
        }
    }
}

