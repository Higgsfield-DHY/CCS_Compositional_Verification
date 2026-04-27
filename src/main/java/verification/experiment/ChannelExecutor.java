package verification.experiment;

import verification.experiment.autosar1_channel.Autosar1ChannelCase;
import verification.experiment.autosar1_channel.Autosar1ChannelCases;
import verification.experiment.autosar2_channel.Autosar2ChannelCase;
import verification.experiment.autosar2_channel.Autosar2ChannelCases;
import verification.experiment.autosar3_channel.Autosar3ChannelCase;
import verification.experiment.autosar3_channel.Autosar3ChannelCases;
import verification.experiment.channel.ChannelExperimentSupport;
import verification.experiment.channel.DirectSystemVerifier;
import verification.experiment.fischer_channel.FischerChannelCase;
import verification.experiment.fischer_channel.FischerChannelCases;
import verification.experiment.pc_channel.PcChannelCase;
import verification.experiment.pc_channel.PcChannelCases;
import verification.experiment.train_gate_channel.TrainGateChannelCase;
import verification.experiment.train_gate_channel.TrainGateChannelCases;
import verification.plugins.SequenceChecker;
import verification.report.AgQueryStep;
import verification.report.AgRunReport;
import verification.report.AgVerdict;
import verification.reset.ResetHeuristicConfig;
import verification.reset.ResetPolicyType;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.uppaal.verify.Verifyta;
import verification.util.ChannelPreprocessor;
import verification.util.PortPreprocessConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChannelExecutor {
    private static final String REAL_ROOT_DIR = "Experiments/RealChannelCases";

    private enum Mode {
        DIRECT_ONLY,
        AG_ONLY,
        COMPARE_ONE,
        SUMMARY_TABLE
    }

    private enum Group {
        PC,
        FISCHER,
        TRAIN_GATE,
        AUTOSAR1,
        AUTOSAR2,
        AUTOSAR3,
        ALL
    }

    public static void main(String[] args) throws Exception {
        // ==================== Mode ====================
        Mode mode = Mode.SUMMARY_TABLE;
        // mode = Mode.DIRECT_ONLY;
        // mode = Mode.AG_ONLY;
        // mode = Mode.COMPARE_ONE;

        // ==================== Group ====================
        Group group = Group.ALL;
        // Recommended order when extending real cases:
        // group = Group.PC;
        // group = Group.TRAIN_GATE;
        // group = Group.FISCHER;
        // group = Group.AUTOSAR1;
        // group = Group.AUTOSAR2;
        // group = Group.AUTOSAR3;

        // ==================== Single Case ====================
        String caseId = "PC_1";
        // caseId = "Fischer2_1";
        // caseId = "Experiment1_1";

        // ==================== Logging ====================
        boolean detailed = false;

        // ==================== Time Budget ====================
        long directTimeoutMs = 20L * 60L * 1000L;
        long agTimeoutMs = 10L * 60L * 1000L;
        ResetPolicyType agResetPolicy = ResetPolicyType.STATIC_SIGMA;
        int repeatCount = 3;

        mode = parseModeArg(args, mode);
        group = parseGroupArg(args, group);
        caseId = parseArg(args, "--case=", caseId);
        detailed = parseBooleanFlag(args, "--detailed", detailed);
        directTimeoutMs = parseLongArg(args, "--direct-timeout-ms=", directTimeoutMs);
        agTimeoutMs = parseLongArg(args, "--ag-timeout-ms=", agTimeoutMs);
        agResetPolicy = parseResetPolicyArg(args, agResetPolicy);
        repeatCount = parseIntArg(args, "--repeat=", repeatCount);

        switch (mode) {
            case DIRECT_ONLY:
                requireSingleGroup(group);
                printModeHeader("DIRECT_ONLY", group, caseId, detailed, directTimeoutMs, agTimeoutMs, agResetPolicy, repeatCount);
                printDirectSummary(runDirectOnly(findCase(group, caseId), directTimeoutMs));
                return;
            case AG_ONLY:
                requireSingleGroup(group);
                printModeHeader("AG_ONLY", group, caseId, detailed, directTimeoutMs, agTimeoutMs, agResetPolicy, repeatCount);
                printAgSummary(runAgOnly(findCase(group, caseId), agTimeoutMs, detailed, agResetPolicy));
                return;
            case COMPARE_ONE:
                requireSingleGroup(group);
                printModeHeader("COMPARE_ONE", group, caseId, detailed, directTimeoutMs, agTimeoutMs, agResetPolicy, repeatCount);
                printCompareSummary(runCompare(findCase(group, caseId), directTimeoutMs, agTimeoutMs, detailed, agResetPolicy));
                return;
            case SUMMARY_TABLE:
                List<ChannelCaseView> selected = loadCases(group);
                printModeHeader("SUMMARY_TABLE", group, null, false, directTimeoutMs, agTimeoutMs, agResetPolicy, repeatCount);
                printRunList(selected);
                List<SummaryRow> rows = runSummary(selected, directTimeoutMs, agTimeoutMs, repeatCount);
                printSummaryTable(rows);
                writeSummaryOutputs(group, rows);
                return;
            default:
                throw new IllegalStateException("Unsupported mode: " + mode);
        }
    }

    private static void requireSingleGroup(Group group) {
        if (group == Group.ALL) {
            throw new IllegalArgumentException("Single-case mode requires a concrete group, not ALL.");
        }
    }

    private static void printModeHeader(String mode, Group group, String caseId,
                                        boolean detailed, long directTimeoutMs, long agTimeoutMs,
                                        ResetPolicyType agResetPolicy, int repeatCount) {
        System.out.println("============================================================");
        System.out.println("Channel Executor");
        System.out.println("Mode             : " + mode);
        System.out.println("Group            : " + group.name());
        if (caseId != null) {
            System.out.println("Case             : " + caseId);
        }
        System.out.println("Detailed         : " + detailed);
        System.out.println("Direct timeout   : " + directTimeoutMs);
        System.out.println("AG timeout       : " + agTimeoutMs);
        String policyLabel = "SUMMARY_TABLE".equals(mode)
                ? "BOTH(STATIC_SIGMA,DYNAMIC_GAMMA)"
                : (agResetPolicy == null ? "EXPERIMENT_DEFAULT" : agResetPolicy.name());
        System.out.println("AG reset policy  : " + policyLabel);
        System.out.println("Repeat count     : " + repeatCount);
        System.out.println("============================================================");
    }

    private static void printRunList(List<ChannelCaseView> selected) {
        System.out.println("Run list:");
        for (ChannelCaseView view : selected) {
            System.out.println("  - " + view.getGroupName() + " / " + view.getCaseId()
                    + " [" + view.getStructureGroup() + ", " + view.getPartitionNote() + ", " + view.getPreprocessLabel() + "]"
                    + " -> " + view.getVerifyGoal());
        }
        System.out.println();
    }

    private static DirectRunResult runDirectOnly(ChannelCaseView view, long timeoutMs) throws Exception {
        DirectRunResult result = new DirectRunResult();
        result.groupName = view.getGroupName();
        result.caseId = view.getCaseId();
        result.sourceCase = view.getSourceCase();
        result.structureGroup = view.getStructureGroup();
        result.partitionNote = view.getPartitionNote();
        result.verifyGoal = view.getVerifyGoal();
        result.m1Desc = view.getM1Desc();
        result.m2Desc = view.getM2Desc();
        result.timeoutMs = timeoutMs;

        long begin = System.currentTimeMillis();
        try {
            final Experiment directExp = ChannelExperimentSupport.instantiateSourceExperiment(view.getSourceCase());
            result.verdict = ChannelExperimentSupport.withVerifytaTimeout(timeoutMs, new ChannelExperimentSupport.CheckedSupplier<AgVerdict>() {
                @Override
                public AgVerdict get() throws Exception {
                    return runDirectTruth(directExp);
                }
            });
            result.elapsedMs = System.currentTimeMillis() - begin;
        } catch (Exception e) {
            result.elapsedMs = System.currentTimeMillis() - begin;
            result.error = summarizeError(e);
        }
        return result;
    }

    private static AgRunResult runAgOnly(ChannelCaseView view, long timeoutMs, boolean detailed,
                                         ResetPolicyType agResetPolicy) throws Exception {
        AgRunResult result = new AgRunResult();
        result.groupName = view.getGroupName();
        result.caseId = view.getCaseId();
        result.sourceCase = view.getSourceCase();
        result.structureGroup = view.getStructureGroup();
        result.partitionNote = view.getPartitionNote();
        result.verifyGoal = view.getVerifyGoal();
        result.m1Desc = view.getM1Desc();
        result.m2Desc = view.getM2Desc();
        result.timeoutMs = timeoutMs;

        Experiment summaryExp = overrideResetPolicy(view.newExperiment(), agResetPolicy);
        result.targetSigma = formatSortedSet(summaryExp.getTargetSigma());
        result.resetSigma = formatSortedSet(summaryExp.getResetSigma());
        result.resetPolicy = summaryExp.getResetPolicyType().name();

        final Experiment agExp = overrideResetPolicy(view.newExperiment(), agResetPolicy);
        final boolean sequenceCheck = hasSequenceChecker(agExp.getSequenceChecker());

        long begin = System.currentTimeMillis();
        try {
            AgRunReport report;
            if (detailed) {
                printAgDetailedHeader(result);
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
            result.elapsedMs = System.currentTimeMillis() - begin;
            result.verdict = report.getVerdict();
            result.cq1Fails = report.getCq1FailCount();
            result.cq2Fails = report.getCq2FailCount();
            result.secondary = summarizeSecondary(report);
            result.finalStates = report.getFinalStateCount();
            result.reportElapsedMs = report.getElapsedMs();
        } catch (Exception e) {
            result.elapsedMs = System.currentTimeMillis() - begin;
            result.error = summarizeError(e);
        }
        return result;
    }

    private static CompareResult runCompare(ChannelCaseView view, long directTimeoutMs,
                                            long agTimeoutMs, boolean detailed,
                                            ResetPolicyType agResetPolicy) throws Exception {
        CompareResult result = new CompareResult();
        result.groupName = view.getGroupName();
        result.caseId = view.getCaseId();
        result.sourceCase = view.getSourceCase();
        result.structureGroup = view.getStructureGroup();
        result.partitionNote = view.getPartitionNote();
        result.verifyGoal = view.getVerifyGoal();
        result.m1Desc = view.getM1Desc();
        result.m2Desc = view.getM2Desc();
        result.directTimeoutMs = directTimeoutMs;
        result.agTimeoutMs = agTimeoutMs;

        Experiment summaryExp = overrideResetPolicy(view.newExperiment(), agResetPolicy);
        result.targetSigma = formatSortedSet(summaryExp.getTargetSigma());
        result.resetSigma = formatSortedSet(summaryExp.getResetSigma());
        result.resetPolicy = summaryExp.getResetPolicyType().name();

        long begin = System.currentTimeMillis();
        try {
            if (detailed) {
                System.out.println(">>> Direct verification start");
            }
            DirectRunResult direct = runDirectOnly(view, directTimeoutMs);
            result.directTruth = direct.verdict == null ? "UNKNOWN" : direct.verdict.name();
            result.directTimeMs = direct.elapsedMs;
            result.directError = direct.error;
            if (detailed) {
                if (direct.error == null) {
                    System.out.println(">>> Direct verification end: " + result.directTruth
                            + " (" + result.directTimeMs + " ms)");
                } else {
                    System.out.println(">>> Direct verification end: " + direct.error);
                }
            }

            if (result.directError == null) {
                AgRunResult ag = runAgOnly(view, agTimeoutMs, detailed, agResetPolicy);
                result.agVerdict = ag.verdict == null ? "UNKNOWN" : ag.verdict.name();
                result.agTimeMs = ag.reportElapsedMs >= 0 ? ag.reportElapsedMs : ag.elapsedMs;
                result.agError = ag.error;
                result.cq1Fails = ag.cq1Fails;
                result.cq2Fails = ag.cq2Fails;
                result.secondary = ag.secondary;
                result.finalStates = ag.finalStates;
            }
        } catch (Exception e) {
            result.agError = summarizeError(e);
        }
        result.totalTimeMs = System.currentTimeMillis() - begin;
        if (result.directError == null && result.agError == null
                && result.directTruth != null && result.directTruth.equals(result.agVerdict)) {
            result.match = true;
        }
        return result;
    }

    private static List<SummaryRow> runSummary(List<ChannelCaseView> selected, long directTimeoutMs,
                                               long agTimeoutMs, int repeatCount) throws Exception {
        List<SummaryRow> rows = new ArrayList<SummaryRow>();
        for (ChannelCaseView view : selected) {
            rows.add(runSummaryRow(view, directTimeoutMs, agTimeoutMs, repeatCount));
        }
        return rows;
    }

    private static void printDirectSummary(DirectRunResult s) {
        System.out.println("------------------------------------------------------------");
        System.out.println("Direct Verification Summary");
        System.out.println("Group          : " + s.groupName);
        System.out.println("Case ID        : " + s.caseId);
        System.out.println("Source         : " + s.sourceCase);
        System.out.println("Structure      : " + s.structureGroup);
        System.out.println("Partition      : " + s.partitionNote);
        System.out.println("Property       : " + s.verifyGoal);
        System.out.println("M1             : " + s.m1Desc);
        System.out.println("M2             : " + s.m2Desc);
        System.out.println("Timeout(ms)    : " + s.timeoutMs);
        if (s.error == null) {
            System.out.println("DirectTruth    : " + s.verdict);
            System.out.println("Status         : PASS");
        } else {
            System.out.println("Status         : " + classifyErrorStatus(s.error));
            System.out.println("Error          : " + s.error);
        }
        System.out.println("Elapsed(ms)    : " + s.elapsedMs);
        System.out.println("------------------------------------------------------------");
    }

    private static void printAgDetailedHeader(AgRunResult summary) {
        System.out.println("------------------------------------------------------------");
        System.out.println("AG log mode : DETAILED");
        System.out.println("group       : " + summary.groupName);
        System.out.println("caseId      : " + summary.caseId);
        System.out.println("source      : " + summary.sourceCase);
        System.out.println("structure   : " + summary.structureGroup);
        System.out.println("partition   : " + summary.partitionNote);
        System.out.println("property    : " + summary.verifyGoal);
        System.out.println("sigma       : " + summary.targetSigma);
        System.out.println("resetPolicy : " + summary.resetPolicy);
        System.out.println("resetSigma  : " + summary.resetSigma);
        System.out.println("timeoutMs   : " + summary.timeoutMs);
        System.out.println("------------------------------------------------------------");
    }

    private static void printAgSummary(AgRunResult s) {
        System.out.println("------------------------------------------------------------");
        System.out.println("AG Summary");
        System.out.println("Group          : " + s.groupName);
        System.out.println("Case ID        : " + s.caseId);
        System.out.println("Source         : " + s.sourceCase);
        System.out.println("Structure      : " + s.structureGroup);
        System.out.println("Partition      : " + s.partitionNote);
        System.out.println("Property       : " + s.verifyGoal);
        System.out.println("M1             : " + s.m1Desc);
        System.out.println("M2             : " + s.m2Desc);
        System.out.println("Target sigma   : " + s.targetSigma);
        System.out.println("Reset sigma    : " + s.resetSigma);
        System.out.println("Reset policy   : " + s.resetPolicy);
        System.out.println("Timeout(ms)    : " + s.timeoutMs);
        if (s.error == null) {
            System.out.println("AGVerdict      : " + s.verdict);
            System.out.println("Status         : PASS");
            System.out.println("CQ1/CQ2 fails  : " + s.cq1Fails + " / " + s.cq2Fails);
            System.out.println("Secondary      : " + s.secondary);
            System.out.println("Final states   : " + s.finalStates);
            System.out.println("AG time(ms)    : " + s.reportElapsedMs);
        } else {
            System.out.println("Status         : " + classifyErrorStatus(s.error));
            System.out.println("Error          : " + s.error);
        }
        System.out.println("Elapsed(ms)    : " + s.elapsedMs);
        System.out.println("------------------------------------------------------------");
    }

    private static void printCompareSummary(CompareResult s) {
        System.out.println("============================================================");
        System.out.println("Channel Compare Summary");
        System.out.println("Group          : " + s.groupName);
        System.out.println("Case ID        : " + s.caseId);
        System.out.println("Source         : " + s.sourceCase);
        System.out.println("Structure      : " + s.structureGroup);
        System.out.println("Partition      : " + s.partitionNote);
        System.out.println("Property       : " + s.verifyGoal);
        System.out.println("M1             : " + s.m1Desc);
        System.out.println("M2             : " + s.m2Desc);
        System.out.println("Target sigma   : " + s.targetSigma);
        System.out.println("Reset sigma    : " + s.resetSigma);
        System.out.println("Reset policy   : " + s.resetPolicy);
        System.out.println("Direct timeout : " + s.directTimeoutMs);
        System.out.println("AG timeout     : " + s.agTimeoutMs);
        if (s.directError == null) {
            System.out.println("DirectTruth    : " + s.directTruth);
            System.out.println("Direct time(ms): " + s.directTimeMs);
        } else {
            System.out.println("DirectTruth    : " + classifyErrorStatus(s.directError));
            System.out.println("Direct error   : " + s.directError);
        }
        if (s.agError == null) {
            System.out.println("AGVerdict      : " + s.agVerdict);
            System.out.println("AG time(ms)    : " + s.agTimeMs);
            System.out.println("CQ1/CQ2 fails  : " + s.cq1Fails + " / " + s.cq2Fails);
            System.out.println("Secondary      : " + s.secondary);
            System.out.println("Final states   : " + s.finalStates);
        } else {
            System.out.println("AGVerdict      : " + classifyErrorStatus(s.agError));
            System.out.println("AG error       : " + s.agError);
        }
        System.out.println("Match          : " + (s.match ? "YES" : "NO"));
        System.out.println("Status         : " + s.getStatus());
        System.out.println("Total time(ms) : " + s.totalTimeMs);
        System.out.println("============================================================");
    }

    private static void printSummaryTable(List<SummaryRow> rows) {
        System.out.println("| Group | Case ID | Valid | UPPAAL Tmean(ms) | AG-Static |Q| |Σ| R Tmean(ms) | AG-Dynamic |Q| |Σ| R Tmean(ms) | Preprocess | Partition |");
        System.out.println("|---|---|---|---:|---|---|");
        for (SummaryRow row : rows) {
            System.out.println("| " + row.groupName
                    + " | " + row.caseId
                    + " | " + row.valid
                    + " | " + formatDirectMetric(row.direct)
                    + " | " + formatAgMetric(row.staticAg)
                    + " | " + formatAgMetric(row.dynamicAg)
                    + " | " + escapeMd(row.preprocess)
                    + " | " + escapeMd(row.partitionNote)
                    + " |");
        }
    }

    private static void writeSummaryOutputs(Group selectedGroup, List<SummaryRow> rows) throws IOException {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        if (selectedGroup == Group.ALL) {
            writeResultBundle(new File(resultDirForGroup(Group.ALL)), rows);
            writePerGroupBundle(Group.PC, rows);
            writePerGroupBundle(Group.TRAIN_GATE, rows);
            writePerGroupBundle(Group.FISCHER, rows);
            writePerGroupBundle(Group.AUTOSAR1, rows);
            writePerGroupBundle(Group.AUTOSAR2, rows);
            writePerGroupBundle(Group.AUTOSAR3, rows);
            writeNamedBundle("MAIN", filterMainRealCases(rows));
            writeNamedBundle("SUPPORT", filterSupportRealCases(rows));
            System.out.println();
            System.out.println("Result bundles written under " + REAL_ROOT_DIR);
            return;
        }
        writeResultBundle(new File(resultDirForGroup(selectedGroup)), rows);
        System.out.println();
        System.out.println("Result bundle written under " + resultDirForGroup(selectedGroup));
    }

    private static void writePerGroupBundle(Group group, List<SummaryRow> rows) throws IOException {
        List<SummaryRow> filtered = filterRowsByGroup(rows, displayGroupName(group));
        if (!filtered.isEmpty()) {
            writeResultBundle(new File(resultDirForGroup(group)), filtered);
        }
    }

    private static void writeNamedBundle(String name, List<SummaryRow> rows) throws IOException {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        writeResultBundle(new File(REAL_ROOT_DIR + "/" + name + "_exp_result"), rows);
    }

    private static List<SummaryRow> filterRowsByGroup(List<SummaryRow> rows, String groupName) {
        List<SummaryRow> filtered = new ArrayList<SummaryRow>();
        for (SummaryRow row : rows) {
            if (groupName.equals(row.groupName)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static List<SummaryRow> filterMainRealCases(List<SummaryRow> rows) {
        List<SummaryRow> filtered = new ArrayList<SummaryRow>();
        for (SummaryRow row : rows) {
            if (isMainRealCase(row)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static List<SummaryRow> filterSupportRealCases(List<SummaryRow> rows) {
        List<SummaryRow> filtered = new ArrayList<SummaryRow>();
        for (SummaryRow row : rows) {
            if (isSupportRealCase(row)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static boolean isMainRealCase(SummaryRow row) {
        if (row == null) {
            return false;
        }
        if ("Train-Gate Channel".equals(row.groupName)) {
            return true;
        }
        return "PC Channel".equals(row.groupName) && "PC_1".equalsIgnoreCase(row.caseId);
    }

    private static boolean isSupportRealCase(SummaryRow row) {
        if (row == null) {
            return false;
        }
        if ("Fischer Channel".equals(row.groupName)) {
            return true;
        }
        return "PC Channel".equals(row.groupName)
                && ("PC_2".equalsIgnoreCase(row.caseId) || "PC_3".equalsIgnoreCase(row.caseId));
    }

    private static void writeResultBundle(File root, List<SummaryRow> rows) throws IOException {
        writeDirectCsv(new File(root, "direct/real_direct.csv"), rows);
        writeAgCsv(new File(root, "ag_static/real_ag_static.csv"), rows, true);
        writeAgCsv(new File(root, "ag_dynamic/real_ag_dynamic.csv"), rows, false);
        writeSummaryCsv(new File(root, "summary/real_summary.csv"), rows);
        writeSummaryMd(new File(root, "summary/real_summary.md"), rows);
    }

    private static void writeDirectCsv(File file, List<SummaryRow> rows) throws IOException {
        ensureDir(file.getParentFile());
        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        try {
            writer.write("group,caseId,sourceCase,property,m1,m2,structureGroup,partition,preprocess,valid,verdict,status,tMeanMs,error");
            writer.newLine();
            for (SummaryRow row : rows) {
                writer.write(csv(row.groupName) + "," + csv(row.caseId) + "," + csv(row.sourceCase) + ","
                        + csv(row.verifyGoal) + "," + csv(row.m1Desc) + "," + csv(row.m2Desc) + ","
                        + csv(row.structureGroup) + "," + csv(row.partitionNote) + "," + csv(row.preprocess) + ","
                        + csv(row.valid) + "," + csv(row.direct.verdict) + "," + csv(row.direct.status) + ","
                        + row.direct.tMeanMs + "," + csv(row.direct.error));
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    private static void writeAgCsv(File file, List<SummaryRow> rows, boolean isStatic) throws IOException {
        ensureDir(file.getParentFile());
        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        try {
            writer.write("group,caseId,sourceCase,property,m1,m2,structureGroup,partition,preprocess,policy,valid,verdict,status,match,tMeanMs,q,sigma,r,error");
            writer.newLine();
            for (SummaryRow row : rows) {
                AgAggregate ag = isStatic ? row.staticAg : row.dynamicAg;
                writer.write(csv(row.groupName) + "," + csv(row.caseId) + "," + csv(row.sourceCase) + ","
                        + csv(row.verifyGoal) + "," + csv(row.m1Desc) + "," + csv(row.m2Desc) + ","
                        + csv(row.structureGroup) + "," + csv(row.partitionNote) + "," + csv(row.preprocess) + ","
                        + csv(ag.policy) + "," + csv(row.valid) + "," + csv(ag.verdict) + "," + csv(ag.status) + ","
                        + csv(resolveMatch(row.direct, ag)) + "," + ag.tMeanMs + "," + ag.q + "," + ag.sigma + ","
                        + ag.r + "," + csv(ag.error));
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    private static void writeSummaryCsv(File file, List<SummaryRow> rows) throws IOException {
        ensureDir(file.getParentFile());
        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        try {
            writer.write("group,caseId,sourceCase,property,m1,m2,structureGroup,partition,preprocess,valid,directVerdict,directStatus,directTMeanMs,staticVerdict,staticStatus,staticMatch,staticTMeanMs,staticQ,staticSigma,staticR,staticSpeedup,dynamicVerdict,dynamicStatus,dynamicMatch,dynamicTMeanMs,dynamicQ,dynamicSigma,dynamicR,dynamicSpeedup");
            writer.newLine();
            for (SummaryRow row : rows) {
                writer.write(csv(row.groupName) + "," + csv(row.caseId) + "," + csv(row.sourceCase) + ","
                        + csv(row.verifyGoal) + "," + csv(row.m1Desc) + "," + csv(row.m2Desc) + ","
                        + csv(row.structureGroup) + "," + csv(row.partitionNote) + "," + csv(row.preprocess) + ","
                        + csv(row.valid) + "," + csv(row.direct.verdict) + "," + csv(row.direct.status) + "," + row.direct.tMeanMs + ","
                        + csv(row.staticAg.verdict) + "," + csv(row.staticAg.status) + "," + csv(resolveMatch(row.direct, row.staticAg)) + ","
                        + row.staticAg.tMeanMs + "," + row.staticAg.q + "," + row.staticAg.sigma + "," + row.staticAg.r + "," + csv(formatSpeedup(row.direct, row.staticAg)) + ","
                        + csv(row.dynamicAg.verdict) + "," + csv(row.dynamicAg.status) + "," + csv(resolveMatch(row.direct, row.dynamicAg)) + ","
                        + row.dynamicAg.tMeanMs + "," + row.dynamicAg.q + "," + row.dynamicAg.sigma + "," + row.dynamicAg.r + "," + csv(formatSpeedup(row.direct, row.dynamicAg)));
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    private static void writeSummaryMd(File file, List<SummaryRow> rows) throws IOException {
        ensureDir(file.getParentFile());
        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        try {
            writer.write("# Real Channel Summary");
            writer.newLine();
            writer.newLine();
            writer.write("| Group | Case ID | Source | Property | M1 | M2 | Valid | Direct Tmean(ms) | Static Q/Sigma/R/T | Dynamic Q/Sigma/R/T | Static Speedup | Dynamic Speedup | Preprocess | Partition |");
            writer.newLine();
            writer.write("|---|---|---|---|---|---|---|---:|---|---|---:|---:|---|---|");
            writer.newLine();
            for (SummaryRow row : rows) {
                writer.write("| " + escapeMd(row.groupName)
                        + " | " + escapeMd(row.caseId)
                        + " | " + escapeMd(row.sourceCase)
                        + " | " + escapeMd(row.verifyGoal)
                        + " | " + escapeMd(row.m1Desc)
                        + " | " + escapeMd(row.m2Desc)
                        + " | " + escapeMd(row.valid)
                        + " | " + formatDirectMetric(row.direct)
                        + " | " + formatAgMetric(row.staticAg)
                        + " | " + formatAgMetric(row.dynamicAg)
                        + " | " + formatSpeedup(row.direct, row.staticAg)
                        + " | " + formatSpeedup(row.direct, row.dynamicAg)
                        + " | " + escapeMd(row.preprocess)
                        + " | " + escapeMd(row.partitionNote)
                        + " |");
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    private static String resultDirForGroup(Group group) {
        return REAL_ROOT_DIR + "/" + group.name() + "_exp_result";
    }

    private static String displayGroupName(Group group) {
        switch (group) {
            case PC:
                return "PC Channel";
            case FISCHER:
                return "Fischer Channel";
            case TRAIN_GATE:
                return "Train-Gate Channel";
            case AUTOSAR1:
                return "AUTOSAR-1 Channel";
            case AUTOSAR2:
                return "AUTOSAR-2 Channel";
            case AUTOSAR3:
                return "AUTOSAR-3 Channel";
            default:
                return "ALL";
        }
    }

    private static SummaryRow runSummaryRow(ChannelCaseView view, long defaultDirectTimeoutMs,
                                            long agTimeoutMs, int repeatCount) throws Exception {
        SummaryRow row = new SummaryRow();
        row.groupName = view.getGroupName();
        row.caseId = view.getCaseId();
        row.sourceCase = view.getSourceCase();
        row.structureGroup = view.getStructureGroup();
        row.partitionNote = view.getPartitionNote();
        row.verifyGoal = view.getVerifyGoal();
        row.m1Desc = view.getM1Desc();
        row.m2Desc = view.getM2Desc();

        Experiment summaryExp = view.newExperiment();
        row.preprocess = view.getPreprocessLabel();
        row.targetSigmaSize = summaryExp.getTargetSigma().size();

        row.direct = aggregateDirect(view, summaryDirectTimeoutMs(view, defaultDirectTimeoutMs), repeatCount);
        row.staticAg = aggregateAg(view, agTimeoutMs, ResetPolicyType.STATIC_SIGMA, repeatCount);
        row.dynamicAg = aggregateAg(view, agTimeoutMs, ResetPolicyType.DYNAMIC_GAMMA, repeatCount);
        row.valid = resolveValidLabel(row.direct, row.staticAg, row.dynamicAg);
        return row;
    }

    private static DirectAggregate aggregateDirect(ChannelCaseView view, long timeoutMs, int repeatCount) throws Exception {
        DirectAggregate aggregate = new DirectAggregate();
        aggregate.timeoutMs = timeoutMs;
        aggregate.repeatCount = repeatCount;

        long totalMs = 0L;
        int successfulRuns = 0;
        String expectedVerdict = null;
        for (int i = 0; i < repeatCount; i++) {
            DirectRunResult run = runDirectOnly(view, timeoutMs);
            if (run.error != null) {
                aggregate.status = classifyDirectAggregateStatus(run.error);
                aggregate.error = run.error;
                if ("ROM".equals(aggregate.status)) {
                    aggregate.verdict = "ROM";
                }
                return aggregate;
            }
            String verdict = run.verdict == null ? "UNKNOWN" : run.verdict.name();
            if (expectedVerdict == null) {
                expectedVerdict = verdict;
            } else if (!expectedVerdict.equals(verdict)) {
                aggregate.status = "MISMATCH";
                aggregate.error = "Direct verification verdict mismatch across repeats";
                return aggregate;
            }
            totalMs += run.elapsedMs;
            successfulRuns++;
        }
        aggregate.status = "PASS";
        aggregate.verdict = expectedVerdict == null ? "UNKNOWN" : expectedVerdict;
        aggregate.tMeanMs = successfulRuns == 0 ? -1L : totalMs / successfulRuns;
        return aggregate;
    }

    private static AgAggregate aggregateAg(ChannelCaseView view, long timeoutMs, ResetPolicyType policy, int repeatCount) throws Exception {
        AgAggregate aggregate = new AgAggregate();
        aggregate.timeoutMs = timeoutMs;
        aggregate.repeatCount = repeatCount;
        aggregate.policy = policy.name();

        long totalMs = 0L;
        String expectedVerdict = null;
        Integer expectedStates = null;
        Integer expectedRefines = null;
        int sigmaSize = -1;

        for (int i = 0; i < repeatCount; i++) {
            AgRunResult run = runAgOnly(view, timeoutMs, false, policy);
            if (run.error != null) {
                aggregate.status = classifyErrorStatus(run.error);
                aggregate.error = run.error;
                return aggregate;
            }

            String verdict = run.verdict == null ? "UNKNOWN" : run.verdict.name();
            int refines = run.cq1Fails + run.cq2Fails;
            if (expectedVerdict == null) {
                expectedVerdict = verdict;
                expectedStates = run.finalStates;
                expectedRefines = refines;
                sigmaSize = countSymbols(run.targetSigma);
            } else if (!expectedVerdict.equals(verdict)
                    || expectedStates == null || expectedStates.intValue() != run.finalStates
                    || expectedRefines == null || expectedRefines.intValue() != refines) {
                aggregate.status = "MISMATCH";
                aggregate.error = "AG result mismatch across repeats";
                return aggregate;
            }
            totalMs += run.reportElapsedMs >= 0 ? run.reportElapsedMs : run.elapsedMs;
        }

        aggregate.status = "PASS";
        aggregate.verdict = expectedVerdict == null ? "UNKNOWN" : expectedVerdict;
        aggregate.q = expectedStates == null ? -1 : expectedStates.intValue();
        aggregate.sigma = sigmaSize;
        aggregate.r = expectedRefines == null ? -1 : expectedRefines.intValue();
        aggregate.tMeanMs = repeatCount == 0 ? -1L : totalMs / repeatCount;
        return aggregate;
    }

    private static String summaryValue(String verdict, String error) {
        if (error != null) {
            return classifyErrorStatus(error);
        }
        return verdict == null ? "UNKNOWN" : verdict;
    }

    private static String formatMs(long ms) {
        return ms >= 0 ? String.valueOf(ms) : "-";
    }

    private static String formatDirectMetric(DirectAggregate aggregate) {
        if (aggregate == null) {
            return "-";
        }
        if ("ROM".equals(aggregate.status)) {
            return "ROM";
        }
        if (!"PASS".equals(aggregate.status)) {
            return aggregate.status;
        }
        return formatMs(aggregate.tMeanMs);
    }

    private static String formatAgMetric(AgAggregate aggregate) {
        if (aggregate == null) {
            return "ERR";
        }
        if (!"PASS".equals(aggregate.status)) {
            return aggregate.status;
        }
        return aggregate.q + " / " + aggregate.sigma + " / " + aggregate.r + " / " + formatMs(aggregate.tMeanMs);
    }

    private static String resolveValidLabel(DirectAggregate direct, AgAggregate staticAg, AgAggregate dynamicAg) {
        if (direct != null && "PASS".equals(direct.status)) {
            return toValidLabel(direct.verdict);
        }
        if (direct != null && "ROM".equals(direct.status)
                && staticAg != null && dynamicAg != null
                && "PASS".equals(staticAg.status) && "PASS".equals(dynamicAg.status)
                && staticAg.verdict != null && staticAg.verdict.equals(dynamicAg.verdict)) {
            return toValidLabel(staticAg.verdict);
        }
        return "UNKNOWN";
    }

    private static String toValidLabel(String verdict) {
        if ("SAFE".equalsIgnoreCase(verdict)) {
            return "Yes";
        }
        if ("UNSAFE".equalsIgnoreCase(verdict)) {
            return "No";
        }
        return "UNKNOWN";
    }

    private static String classifyDirectAggregateStatus(String error) {
        if (error != null && error.contains(Verifyta.VERIFYTA_TIMEOUT)) {
            return "ROM";
        }
        return "ERROR";
    }

    private static int countSymbols(String formattedSigma) {
        if (formattedSigma == null) {
            return -1;
        }
        String trimmed = formattedSigma.trim();
        if ("[]".equals(trimmed)) {
            return 0;
        }
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String body = trimmed.substring(1, trimmed.length() - 1).trim();
            if (body.isEmpty()) {
                return 0;
            }
            return body.split(",").length;
        }
        return -1;
    }

    private static long summaryDirectTimeoutMs(ChannelCaseView view, long fallback) {
        if ("AUTOSAR-2 Channel".equals(view.getGroupName())) {
            return 5L * 60L * 1000L;
        }
        return fallback;
    }

    private static String escapeMd(String value) {
        if (value == null) {
            return "-";
        }
        return value.replace("|", "\\|");
    }

    private static String resolveMatch(DirectAggregate direct, AgAggregate ag) {
        if (direct == null || ag == null) {
            return "NO";
        }
        if (!"PASS".equals(direct.status) || !"PASS".equals(ag.status)) {
            return "NO";
        }
        return direct.verdict != null && direct.verdict.equals(ag.verdict) ? "YES" : "NO";
    }

    private static String formatSpeedup(DirectAggregate direct, AgAggregate ag) {
        if (direct == null || ag == null) {
            return "-";
        }
        if (!"PASS".equals(direct.status) || !"PASS".equals(ag.status)) {
            return "-";
        }
        if (direct.tMeanMs <= 0 || ag.tMeanMs <= 0) {
            return "-";
        }
        double speedup = (double) direct.tMeanMs / (double) ag.tMeanMs;
        return String.format(Locale.ROOT, "%.2f", speedup);
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static void ensureDir(File dir) throws IOException {
        if (dir == null) {
            return;
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }
    }

    private static boolean hasSequenceChecker(List<SequenceChecker> sequenceCheckers) {
        return sequenceCheckers != null && !sequenceCheckers.isEmpty();
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

    private static String describePreprocess(Experiment experiment) {
        if (experiment == null || !experiment.isPortActionMode()) {
            return "NONE";
        }
        PortPreprocessConfig config = experiment.getPortPreprocessConfig();
        if (config == null || config.isEmpty()) {
            return "NONE";
        }
        return config.getMode().name();
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

    private static String summarizeError(Exception e) {
        return e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage());
    }

    private static String classifyErrorStatus(String error) {
        if (error != null && error.contains(Verifyta.VERIFYTA_TIMEOUT)) {
            return "TIMEOUT";
        }
        return "ERROR";
    }

    private static Mode parseModeArg(String[] args, Mode fallback) {
        String raw = parseArg(args, "--mode=", null);
        if (raw == null) {
            return fallback;
        }
        return Mode.valueOf(raw.trim().toUpperCase().replace('-', '_'));
    }

    private static Group parseGroupArg(String[] args, Group fallback) {
        String raw = parseArg(args, "--group=", null);
        if (raw == null) {
            return fallback;
        }
        return Group.valueOf(raw.trim().toUpperCase());
    }

    private static String parseArg(String[] args, String prefix, String fallback) {
        if (args == null) {
            return fallback;
        }
        for (String arg : args) {
            if (arg != null && arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return fallback;
    }

    private static long parseLongArg(String[] args, String prefix, long fallback) {
        String raw = parseArg(args, prefix, null);
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        return Long.parseLong(raw.trim());
    }

    private static int parseIntArg(String[] args, String prefix, int fallback) {
        String raw = parseArg(args, prefix, null);
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        return Integer.parseInt(raw.trim());
    }

    private static ResetPolicyType parseResetPolicyArg(String[] args, ResetPolicyType fallback) {
        String raw = parseArg(args, "--reset-policy=", null);
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        return ResetPolicyType.valueOf(raw.trim().toUpperCase());
    }

    private static boolean parseBooleanFlag(String[] args, String flag, boolean fallback) {
        if (args == null) {
            return fallback;
        }
        for (String arg : args) {
            if (flag.equalsIgnoreCase(arg)) {
                return true;
            }
            if (("--no-" + flag.substring(2)).equalsIgnoreCase(arg)) {
                return false;
            }
        }
        return fallback;
    }

    private static ChannelCaseView findCase(Group group, String caseId) {
        for (ChannelCaseView view : loadCases(group)) {
            if (view.getCaseId().equalsIgnoreCase(caseId)) {
                return view;
            }
        }
        throw new IllegalArgumentException("Unknown case for " + group + ": " + caseId);
    }

    private static List<ChannelCaseView> loadCases(Group group) {
        List<ChannelCaseView> all = new ArrayList<ChannelCaseView>();
        if (group == Group.PC || group == Group.ALL) {
            for (PcChannelCase c : PcChannelCases.allCases()) {
                all.add(new PcCaseView(c));
            }
        }
        if (group == Group.TRAIN_GATE || group == Group.ALL) {
            for (TrainGateChannelCase c : TrainGateChannelCases.allCases()) {
                all.add(new TrainGateCaseView(c));
            }
        }
        if (group == Group.FISCHER || group == Group.ALL) {
            for (FischerChannelCase c : FischerChannelCases.allCases()) {
                all.add(new FischerCaseView(c));
            }
        }
        if (group == Group.AUTOSAR1 || group == Group.ALL) {
            for (Autosar1ChannelCase c : Autosar1ChannelCases.allCases()) {
                all.add(new Autosar1CaseView(c));
            }
        }
        if (group == Group.AUTOSAR2 || group == Group.ALL) {
            for (Autosar2ChannelCase c : Autosar2ChannelCases.allCases()) {
                all.add(new Autosar2CaseView(c));
            }
        }
        if (group == Group.AUTOSAR3 || group == Group.ALL) {
            for (Autosar3ChannelCase c : Autosar3ChannelCases.allCases()) {
                all.add(new Autosar3CaseView(c));
            }
        }
        return all;
    }

    private static Experiment overrideResetPolicy(final Experiment delegate, final ResetPolicyType forcedPolicy) {
        if (delegate == null || forcedPolicy == null || delegate.getResetPolicyType() == forcedPolicy) {
            return delegate;
        }
        return new Experiment() {
            @Override
            public String getStatement() {
                return delegate.getStatement();
            }

            @Override
            public java.util.Map<String, Boolean> getSyncSendMap() {
                return delegate.getSyncSendMap();
            }

            @Override
            public java.util.Set<String> getResetSigma() {
                return delegate.getResetSigma();
            }

            @Override
            public java.util.List<Template> getM1() {
                return delegate.getM1();
            }

            @Override
            public java.util.List<Template> getM2() throws IOException {
                return delegate.getM2();
            }

            @Override
            public String getNtaPath() {
                return delegate.getNtaPath();
            }

            @Override
            public Declaration getGlobalDeclaration() {
                return delegate.getGlobalDeclaration();
            }

            @Override
            public java.util.List<SequenceChecker> getSequenceChecker() {
                return delegate.getSequenceChecker();
            }

            @Override
            public boolean isPortActionMode() {
                return delegate.isPortActionMode();
            }

            @Override
            public java.util.Set<String> getTargetSigma() {
                return delegate.getTargetSigma();
            }

            @Override
            public java.util.Map<String, String> getM1RenameMap() {
                return delegate.getM1RenameMap();
            }

            @Override
            public java.util.Map<String, String> getM2RenameMap() {
                return delegate.getM2RenameMap();
            }

            @Override
            public verification.util.PrimeSplitConfig getPrimeSplitConfig() {
                return delegate.getPrimeSplitConfig();
            }

            @Override
            public verification.util.PortPreprocessConfig getPortPreprocessConfig() {
                return delegate.getPortPreprocessConfig();
            }

            @Override
            public verification.frame.Cq2Mode getCq2Mode() {
                return delegate.getCq2Mode();
            }

            @Override
            public ResetPolicyType getResetPolicyType() {
                return forcedPolicy;
            }

            @Override
            public ResetHeuristicConfig getResetHeuristicConfig() {
                return delegate.getResetHeuristicConfig();
            }
        };
    }

    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private interface ChannelCaseView {
        String getGroupName();
        String getCaseId();
        String getSourceCase();
        String getStructureGroup();
        String getPartitionNote();
        String getPreprocessLabel();
        String getM1Desc();
        String getM2Desc();
        String getVerifyGoal();
        Experiment newExperiment();
    }

    private static class Autosar1CaseView implements ChannelCaseView {
        private final Autosar1ChannelCase delegate;

        private Autosar1CaseView(Autosar1ChannelCase delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getGroupName() { return "AUTOSAR-1 Channel"; }
        @Override
        public String getCaseId() { return delegate.getCaseId(); }
        @Override
        public String getSourceCase() { return delegate.getSourceCase(); }
        @Override
        public String getStructureGroup() { return delegate.getStructureGroup(); }
        @Override
        public String getPartitionNote() { return delegate.getPartitionNote(); }
        @Override
        public String getPreprocessLabel() { return describePreprocess(delegate.newExperiment()); }
        @Override
        public String getM1Desc() { return delegate.getM1Desc(); }
        @Override
        public String getM2Desc() { return delegate.getM2Desc(); }
        @Override
        public String getVerifyGoal() { return delegate.getVerifyGoal(); }
        @Override
        public Experiment newExperiment() { return delegate.newExperiment(); }
    }

    private static class PcCaseView implements ChannelCaseView {
        private final PcChannelCase delegate;

        private PcCaseView(PcChannelCase delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getGroupName() { return "PC Channel"; }
        @Override
        public String getCaseId() { return delegate.getCaseId(); }
        @Override
        public String getSourceCase() { return delegate.getSourceCase(); }
        @Override
        public String getStructureGroup() { return delegate.getStructureGroup(); }
        @Override
        public String getPartitionNote() { return delegate.getPartitionNote(); }
        @Override
        public String getPreprocessLabel() { return describePreprocess(delegate.newExperiment()); }
        @Override
        public String getM1Desc() { return delegate.getM1Desc(); }
        @Override
        public String getM2Desc() { return delegate.getM2Desc(); }
        @Override
        public String getVerifyGoal() { return delegate.getVerifyGoal(); }
        @Override
        public Experiment newExperiment() { return delegate.newExperiment(); }
    }

    private static class TrainGateCaseView implements ChannelCaseView {
        private final TrainGateChannelCase delegate;

        private TrainGateCaseView(TrainGateChannelCase delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getGroupName() { return "Train-Gate Channel"; }
        @Override
        public String getCaseId() { return delegate.getCaseId(); }
        @Override
        public String getSourceCase() { return delegate.getSourceCase(); }
        @Override
        public String getStructureGroup() { return delegate.getStructureGroup(); }
        @Override
        public String getPartitionNote() { return delegate.getPartitionNote(); }
        @Override
        public String getPreprocessLabel() { return describePreprocess(delegate.newExperiment()); }
        @Override
        public String getM1Desc() { return delegate.getM1Desc(); }
        @Override
        public String getM2Desc() { return delegate.getM2Desc(); }
        @Override
        public String getVerifyGoal() { return delegate.getVerifyGoal(); }
        @Override
        public Experiment newExperiment() { return delegate.newExperiment(); }
    }

    private static class Autosar2CaseView implements ChannelCaseView {
        private final Autosar2ChannelCase delegate;

        private Autosar2CaseView(Autosar2ChannelCase delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getGroupName() { return "AUTOSAR-2 Channel"; }
        @Override
        public String getCaseId() { return delegate.getCaseId(); }
        @Override
        public String getSourceCase() { return delegate.getSourceCase(); }
        @Override
        public String getStructureGroup() { return delegate.getStructureGroup(); }
        @Override
        public String getPartitionNote() { return delegate.getPartitionNote(); }
        @Override
        public String getPreprocessLabel() { return describePreprocess(delegate.newExperiment()); }
        @Override
        public String getM1Desc() { return delegate.getM1Desc(); }
        @Override
        public String getM2Desc() { return delegate.getM2Desc(); }
        @Override
        public String getVerifyGoal() { return delegate.getVerifyGoal(); }
        @Override
        public Experiment newExperiment() { return delegate.newExperiment(); }
    }

    private static class Autosar3CaseView implements ChannelCaseView {
        private final Autosar3ChannelCase delegate;

        private Autosar3CaseView(Autosar3ChannelCase delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getGroupName() { return "AUTOSAR-3 Channel"; }
        @Override
        public String getCaseId() { return delegate.getCaseId(); }
        @Override
        public String getSourceCase() { return delegate.getSourceCase(); }
        @Override
        public String getStructureGroup() { return delegate.getStructureGroup(); }
        @Override
        public String getPartitionNote() { return delegate.getPartitionNote(); }
        @Override
        public String getPreprocessLabel() { return describePreprocess(delegate.newExperiment()); }
        @Override
        public String getM1Desc() { return delegate.getM1Desc(); }
        @Override
        public String getM2Desc() { return delegate.getM2Desc(); }
        @Override
        public String getVerifyGoal() { return delegate.getVerifyGoal(); }
        @Override
        public Experiment newExperiment() { return delegate.newExperiment(); }
    }

    private static class FischerCaseView implements ChannelCaseView {
        private final FischerChannelCase delegate;

        private FischerCaseView(FischerChannelCase delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getGroupName() { return "Fischer Channel"; }
        @Override
        public String getCaseId() { return delegate.getCaseId(); }
        @Override
        public String getSourceCase() { return delegate.getSourceCase(); }
        @Override
        public String getStructureGroup() { return delegate.getStructureGroup(); }
        @Override
        public String getPartitionNote() { return delegate.getPartitionNote(); }
        @Override
        public String getPreprocessLabel() { return describePreprocess(delegate.newExperiment()); }
        @Override
        public String getM1Desc() { return delegate.getM1Desc(); }
        @Override
        public String getM2Desc() { return delegate.getM2Desc(); }
        @Override
        public String getVerifyGoal() { return delegate.getVerifyGoal(); }
        @Override
        public Experiment newExperiment() { return delegate.newExperiment(); }
    }

    private static class DirectRunResult {
        private String groupName;
        private String caseId;
        private String sourceCase;
        private String structureGroup;
        private String partitionNote;
        private String verifyGoal;
        private String m1Desc;
        private String m2Desc;
        private long timeoutMs;
        private long elapsedMs = -1L;
        private AgVerdict verdict;
        private String error;
    }

    private static class AgRunResult {
        private String groupName;
        private String caseId;
        private String sourceCase;
        private String structureGroup;
        private String partitionNote;
        private String verifyGoal;
        private String m1Desc;
        private String m2Desc;
        private String targetSigma = "[]";
        private String resetSigma = "[]";
        private String resetPolicy = "STATIC_SIGMA";
        private long timeoutMs;
        private long elapsedMs = -1L;
        private long reportElapsedMs = -1L;
        private AgVerdict verdict = AgVerdict.UNKNOWN;
        private int cq1Fails;
        private int cq2Fails;
        private String secondary = "-";
        private int finalStates = -1;
        private String error;
    }

    private static class CompareResult {
        private String groupName;
        private String caseId;
        private String sourceCase;
        private String structureGroup;
        private String partitionNote;
        private String verifyGoal;
        private String m1Desc;
        private String m2Desc;
        private String targetSigma = "[]";
        private String resetSigma = "[]";
        private String resetPolicy = "STATIC_SIGMA";
        private long directTimeoutMs;
        private long agTimeoutMs;
        private String directTruth = "UNKNOWN";
        private String agVerdict = "UNKNOWN";
        private boolean match;
        private int cq1Fails;
        private int cq2Fails;
        private String secondary = "-";
        private int finalStates = -1;
        private long directTimeMs = -1L;
        private long agTimeMs = -1L;
        private long totalTimeMs = -1L;
        private String directError;
        private String agError;

        private String getStatus() {
            if (directError != null) {
                return classifyErrorStatus(directError);
            }
            if (agError != null) {
                return classifyErrorStatus(agError);
            }
            return match ? "PASS" : "MISMATCH";
        }
    }

    private static class SummaryRow {
        private String groupName;
        private String caseId;
        private String sourceCase;
        private String structureGroup;
        private String partitionNote;
        private String verifyGoal;
        private String m1Desc;
        private String m2Desc;
        private String preprocess;
        private int targetSigmaSize;
        private String valid;
        private DirectAggregate direct;
        private AgAggregate staticAg;
        private AgAggregate dynamicAg;
    }

    private static class DirectAggregate {
        private long timeoutMs;
        private int repeatCount;
        private String status = "UNKNOWN";
        private String verdict = "UNKNOWN";
        private long tMeanMs = -1L;
        private String error;
    }

    private static class AgAggregate {
        private String policy;
        private long timeoutMs;
        private int repeatCount;
        private String status = "UNKNOWN";
        private String verdict = "UNKNOWN";
        private int q = -1;
        private int sigma = -1;
        private int r = -1;
        private long tMeanMs = -1L;
        private String error;
    }
}
