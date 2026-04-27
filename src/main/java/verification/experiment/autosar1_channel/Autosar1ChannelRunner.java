package verification.experiment.autosar1_channel;

import verification.experiment.Experiment;
import verification.experiment.channel.ChannelExperimentSupport;
import verification.experiment.channel.DirectSystemVerifier;
import verification.plugins.SequenceChecker;
import verification.report.AgQueryStep;
import verification.report.AgRunReport;
import verification.report.AgVerdict;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.uppaal.verify.Verifyta;
import verification.util.ChannelPreprocessor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class Autosar1ChannelRunner {
    private Autosar1ChannelRunner() {
    }

    private enum LogMode {
        DETAILED,
        SUMMARY
    }

    public static void main(String[] args) throws IOException {
        String presetCase = "Experiment1_single_1";
        // presetCase = "Experiment1_single_2";
        // presetCase = "Experiment1_single_3";
        // presetCase = "Experiment1_single_4";
        // presetCase = "Experiment1_1";
        // presetCase = "Experiment1_2";
        // presetCase = "Experiment1_3";
        // presetCase = "Experiment1_4";
        // presetCase = "Experiment1_5";
        // presetCase = "Experiment1_6";

        long timeoutMs = ChannelExperimentSupport.parseTimeoutMs(args);
        String caseArg = parseArg(args, "--case=");
        String selected = caseArg == null ? presetCase : caseArg;
        Autosar1ChannelCase scenario = Autosar1ChannelCases.findById(selected);
        if (scenario == null) {
            throw new IllegalArgumentException("Unknown case: " + selected
                    + ". Available: " + Autosar1ChannelCases.availableCases());
        }
        RunSummary summary = runCase(scenario, LogMode.DETAILED, timeoutMs);
        printSummary(summary);
        if (!"PASS".equals(summary.getStatus())) {
            System.exit(1);
        }
    }

    public static RunSummary runCaseById(String caseId) throws IOException {
        Autosar1ChannelCase scenario = Autosar1ChannelCases.findById(caseId);
        if (scenario == null) {
            throw new IllegalArgumentException("Unknown case: " + caseId
                    + ". Available: " + Autosar1ChannelCases.availableCases());
        }
        return runCase(scenario, LogMode.SUMMARY, ChannelExperimentSupport.DEFAULT_TIMEOUT_MS);
    }

    public static RunSummary runCase(Autosar1ChannelCase scenario) throws IOException {
        return runCase(scenario, LogMode.SUMMARY, ChannelExperimentSupport.DEFAULT_TIMEOUT_MS);
    }

    public static RunSummary runCase(Autosar1ChannelCase scenario, long timeoutMs) throws IOException {
        return runCase(scenario, LogMode.SUMMARY, timeoutMs);
    }

    public static RunSummary runCaseDetailedById(String caseId) throws IOException {
        Autosar1ChannelCase scenario = Autosar1ChannelCases.findById(caseId);
        if (scenario == null) {
            throw new IllegalArgumentException("Unknown case: " + caseId
                    + ". Available: " + Autosar1ChannelCases.availableCases());
        }
        return runCase(scenario, LogMode.DETAILED, ChannelExperimentSupport.DEFAULT_TIMEOUT_MS);
    }

    public static RunSummary runCaseDetailed(Autosar1ChannelCase scenario) throws IOException {
        return runCase(scenario, LogMode.DETAILED, ChannelExperimentSupport.DEFAULT_TIMEOUT_MS);
    }

    public static RunSummary runCaseDetailed(Autosar1ChannelCase scenario, long timeoutMs) throws IOException {
        return runCase(scenario, LogMode.DETAILED, timeoutMs);
    }

    public static RunSummary runCaseDetailed(String caseId, long timeoutMs) throws IOException {
        Autosar1ChannelCase scenario = Autosar1ChannelCases.findById(caseId);
        if (scenario == null) {
            throw new IllegalArgumentException("Unknown case: " + caseId
                    + ". Available: " + Autosar1ChannelCases.availableCases());
        }
        return runCase(scenario, LogMode.DETAILED, timeoutMs);
    }

    public static void runCaseWithSummary(String caseId) throws IOException {
        runCaseWithSummary(caseId, ChannelExperimentSupport.DEFAULT_TIMEOUT_MS);
    }

    public static void runCaseWithSummary(String caseId, long timeoutMs) throws IOException {
        RunSummary summary = runCaseDetailed(caseId, timeoutMs);
        printSummary(summary);
        if (!"PASS".equals(summary.getStatus())) {
            throw new IllegalStateException("AG result mismatches direct truth or run failed: " + caseId);
        }
    }

    private static RunSummary runCase(Autosar1ChannelCase scenario, LogMode logMode, long timeoutMs) throws IOException {
        RunSummary summary = new RunSummary();
        summary.setCaseId(scenario.getCaseId());
        summary.setSourceCase(scenario.getSourceCase());
        summary.setStructureGroup(scenario.getStructureGroup());
        summary.setPartitionNote(scenario.getPartitionNote());
        summary.setVerifyGoal(scenario.getVerifyGoal());
        summary.setM1Desc(scenario.getM1Desc());
        summary.setM2Desc(scenario.getM2Desc());
        summary.setLogMode(logMode.name());
        summary.setTimeoutMs(timeoutMs);

        long begin = System.currentTimeMillis();
        try {
            final Experiment summaryExp = scenario.newExperiment();
            summary.setTargetSigma(formatSortedSet(summaryExp.getTargetSigma()));
            summary.setResetSigma(formatSortedSet(summaryExp.getResetSigma()));
            summary.setResetPolicy("STATIC_SIGMA");
            final Experiment directExp = ChannelExperimentSupport.instantiateSourceExperiment(scenario.getSourceCase());

            long directBegin = System.currentTimeMillis();
            AgVerdict directTruth = ChannelExperimentSupport.withVerifytaTimeout(timeoutMs, new ChannelExperimentSupport.CheckedSupplier<AgVerdict>() {
                @Override
                public AgVerdict get() throws Exception {
                    return runQuietly(new ThrowingSupplier<AgVerdict>() {
                        @Override
                        public AgVerdict get() throws Exception {
                            return runDirectTruth(directExp);
                        }
                    });
                }
            });
            summary.setDirectTimeMs(System.currentTimeMillis() - directBegin);
            summary.setDirectTruth(directTruth.name());

            final Experiment agExp = scenario.newExperiment();
            boolean sequenceCheck = hasSequenceChecker(agExp.getSequenceChecker());
            AgRunReport report;
            if (logMode == LogMode.DETAILED) {
                printDetailedHeader(summary);
                System.out.println(">>> AG start: observation table init");
                System.out.println(">>> AG loop: EQ/CQ");
                report = ChannelExperimentSupport.withVerifytaTimeout(timeoutMs, new ChannelExperimentSupport.CheckedSupplier<AgRunReport>() {
                    @Override
                    public AgRunReport get() throws Exception {
                        return agExp.executeWithReport(true, false, sequenceCheck, 1);
                    }
                });
                System.out.println(">>> AG end: final hypothesis/verdict");
            } else {
                report = ChannelExperimentSupport.withVerifytaTimeout(timeoutMs, new ChannelExperimentSupport.CheckedSupplier<AgRunReport>() {
                    @Override
                    public AgRunReport get() throws Exception {
                        return runQuietly(new ThrowingSupplier<AgRunReport>() {
                            @Override
                            public AgRunReport get() throws Exception {
                                return agExp.executeWithReport(true, false, sequenceCheck, 1);
                            }
                        });
                    }
                });
            }
            if (report == null) {
                throw new IllegalStateException("AG report is null.");
            }
            summary.setAgVerdict(report.getVerdict().name());
            summary.setMatch(report.getVerdict() == directTruth);
            summary.setCq1Fails(report.getCq1FailCount());
            summary.setCq2Fails(report.getCq2FailCount());
            summary.setSecondary(summarizeSecondary(report));
            summary.setFinalStates(report.getFinalStateCount());
            summary.setTimeMs(report.getElapsedMs());
        } catch (Exception e) {
            summary.setError(e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage()));
            summary.setMatch(false);
        }
        summary.setWallTimeMs(System.currentTimeMillis() - begin);
        return summary;
    }

    private static void printDetailedHeader(RunSummary summary) {
        System.out.println("------------------------------------------------------------");
        System.out.println("Log mode    : DETAILED");
        System.out.println("caseId      : " + summary.getCaseId());
        System.out.println("source      : " + summary.getSourceCase());
        System.out.println("structure   : " + summary.getStructureGroup());
        System.out.println("partition   : " + summary.getPartitionNote());
        System.out.println("property    : " + summary.getVerifyGoal());
        System.out.println("sigma       : " + summary.getTargetSigma());
        System.out.println("resetPolicy : " + summary.getResetPolicy());
        System.out.println("resetSigma  : " + summary.getResetSigma());
        System.out.println("timeoutMs   : " + summary.getTimeoutMs());
        System.out.println("------------------------------------------------------------");
    }

    public static void printSummary(RunSummary s) {
        System.out.println("============================================================");
        System.out.println("AUTOSAR-1 Channel Summary");
        System.out.println("Log mode       : " + s.getLogMode());
        System.out.println("Case ID        : " + s.getCaseId());
        System.out.println("Source         : " + s.getSourceCase());
        System.out.println("Structure      : " + s.getStructureGroup());
        System.out.println("Partition      : " + s.getPartitionNote());
        System.out.println("Property       : " + s.getVerifyGoal());
        System.out.println("M1             : " + s.getM1Desc());
        System.out.println("M2             : " + s.getM2Desc());
        System.out.println("Target sigma   : " + s.getTargetSigma());
        System.out.println("Reset sigma    : " + s.getResetSigma());
        System.out.println("Reset policy   : " + s.getResetPolicy());
        System.out.println("Timeout(ms)    : " + s.getTimeoutMs());
        if (s.getError() == null) {
            System.out.println("DirectTruth    : " + s.getDirectTruth());
            System.out.println("AGVerdict      : " + s.getAgVerdict());
            System.out.println("Match          : " + (s.isMatch() ? "YES" : "NO"));
            System.out.println("Status         : " + s.getStatus());
            System.out.println("Direct time(ms): " + s.getDirectTimeMs());
            System.out.println("CQ1/CQ2 fails  : " + s.getCq1Fails() + " / " + s.getCq2Fails());
            System.out.println("Secondary      : " + s.getSecondary());
            System.out.println("Final states   : " + s.getFinalStates());
            System.out.println("AG time(ms)    : " + s.getTimeMs());
        } else {
            System.out.println("Result         : " + s.getStatus());
            System.out.println("Error          : " + s.getError());
        }
        System.out.println("Total time(ms) : " + s.getWallTimeMs());
        System.out.println("============================================================");
    }

    private static AgVerdict runDirectTruth(Experiment experiment) throws IOException {
        Declaration globalDeclaration = experiment.getGlobalDeclaration();
        List<Template> m1 = experiment.getM1();
        List<Template> m2 = experiment.getM2();
        if (experiment.isPortActionMode()) {
            ChannelPreprocessor.preprocessPortMode(
                    globalDeclaration, m1, m2, experiment.getM1RenameMap(), experiment.getM2RenameMap(),
                    experiment.getTargetSigma(), experiment.getPortPreprocessConfig());
        }
        return DirectSystemVerifier.verify(globalDeclaration, m1, m2, experiment.getStatement());
    }

    private static boolean hasSequenceChecker(List<SequenceChecker> sequenceCheckers) {
        return sequenceCheckers != null && !sequenceCheckers.isEmpty();
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

    private static String formatSortedSet(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return "[]";
        }
        List<String> list = new ArrayList<String>(set);
        Collections.sort(list);
        return list.toString();
    }

    private static String parseArg(String[] args, String prefix) {
        if (args == null) {
            return null;
        }
        for (String arg : args) {
            if (arg != null && arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return null;
    }

    private static <T> T runQuietly(ThrowingSupplier<T> action) throws Exception {
        PrintStream originalOut = System.out;
        PrintStream quiet = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        });
        try {
            System.setOut(quiet);
            return action.get();
        } finally {
            System.setOut(originalOut);
            quiet.close();
        }
    }

    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    public static class RunSummary {
        private String caseId;
        private String sourceCase;
        private String verifyGoal;
        private String structureGroup = "-";
        private String partitionNote = "-";
        private String m1Desc;
        private String m2Desc;
        private String targetSigma = "[]";
        private String resetSigma = "[]";
        private String resetPolicy = "STATIC_SIGMA";
        private String directTruth = "UNKNOWN";
        private String agVerdict = "UNKNOWN";
        private String logMode = "SUMMARY";
        private boolean match;
        private int cq1Fails;
        private int cq2Fails;
        private String secondary = "-";
        private int finalStates = -1;
        private long timeoutMs = ChannelExperimentSupport.DEFAULT_TIMEOUT_MS;
        private long directTimeMs = -1L;
        private long timeMs = -1L;
        private long wallTimeMs = -1L;
        private String error;

        public String getCaseId() { return caseId; }
        public void setCaseId(String caseId) { this.caseId = caseId; }
        public String getSourceCase() { return sourceCase; }
        public void setSourceCase(String sourceCase) { this.sourceCase = sourceCase; }
        public String getVerifyGoal() { return verifyGoal; }
        public void setVerifyGoal(String verifyGoal) { this.verifyGoal = verifyGoal; }
        public String getStructureGroup() { return structureGroup; }
        public void setStructureGroup(String structureGroup) { this.structureGroup = structureGroup; }
        public String getPartitionNote() { return partitionNote; }
        public void setPartitionNote(String partitionNote) { this.partitionNote = partitionNote; }
        public String getM1Desc() { return m1Desc; }
        public void setM1Desc(String m1Desc) { this.m1Desc = m1Desc; }
        public String getM2Desc() { return m2Desc; }
        public void setM2Desc(String m2Desc) { this.m2Desc = m2Desc; }
        public String getTargetSigma() { return targetSigma; }
        public void setTargetSigma(String targetSigma) { this.targetSigma = targetSigma; }
        public String getResetSigma() { return resetSigma; }
        public void setResetSigma(String resetSigma) { this.resetSigma = resetSigma; }
        public String getResetPolicy() { return resetPolicy; }
        public void setResetPolicy(String resetPolicy) { this.resetPolicy = resetPolicy; }
        public String getDirectTruth() { return directTruth; }
        public void setDirectTruth(String directTruth) { this.directTruth = directTruth; }
        public String getAgVerdict() { return agVerdict; }
        public void setAgVerdict(String agVerdict) { this.agVerdict = agVerdict; }
        public String getLogMode() { return logMode; }
        public void setLogMode(String logMode) { this.logMode = logMode; }
        public boolean isMatch() { return match; }
        public void setMatch(boolean match) { this.match = match; }
        public int getCq1Fails() { return cq1Fails; }
        public void setCq1Fails(int cq1Fails) { this.cq1Fails = cq1Fails; }
        public int getCq2Fails() { return cq2Fails; }
        public void setCq2Fails(int cq2Fails) { this.cq2Fails = cq2Fails; }
        public String getSecondary() { return secondary; }
        public void setSecondary(String secondary) { this.secondary = secondary; }
        public int getFinalStates() { return finalStates; }
        public void setFinalStates(int finalStates) { this.finalStates = finalStates; }
        public long getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
        public long getDirectTimeMs() { return directTimeMs; }
        public void setDirectTimeMs(long directTimeMs) { this.directTimeMs = directTimeMs; }
        public long getTimeMs() { return timeMs; }
        public void setTimeMs(long timeMs) { this.timeMs = timeMs; }
        public long getWallTimeMs() { return wallTimeMs; }
        public void setWallTimeMs(long wallTimeMs) { this.wallTimeMs = wallTimeMs; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public String getStatus() {
            if (error != null) {
                return error.contains(Verifyta.VERIFYTA_TIMEOUT) ? "TIMEOUT" : "ERROR";
            }
            return match ? "PASS" : "MISMATCH";
        }
    }
}
