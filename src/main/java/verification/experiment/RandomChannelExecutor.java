package verification.experiment;

import verification.experiment.channel.ChannelExperimentSupport;
import verification.experiment.channel.DirectSystemVerifier;
import verification.experiment.random_channel.RandomChannelCase;
import verification.experiment.random_channel.RandomChannelCaseGenerator;
import verification.experiment.random_channel.RandomChannelCases;
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
import verification.util.PrimeSplitConfig;

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
import java.util.Map;
import java.util.Set;

public class RandomChannelExecutor {
    private enum Mode {
        GENERATE_CASES,
        DIRECT_ONLY,
        AG_ONLY,
        COMPARE_ONE,
        SUMMARY_TABLE
    }

/*
要让 AG 比 direct 快，随机生成的案例必须满足:
1.整体系统状态空间大,也就是 M2 要足够大.
2.接口字母表小.
3.环境可被小假设概括,即虽然 M2 很大,但它对 M1 暴露出的行为要规整,最终假设状态应该小.
4.性质以安全性质为主,避免几步就找到反例的浅层.
5.复杂性主要堆在 M2 内部,让 M2 内部有多组件多时钟多计数状态,但对 M1 只通过少数接口.
6.M1 要小、性质要局部.
*/

    public static void main(String[] args) throws Exception {
        Mode mode = Mode.SUMMARY_TABLE;
        RandomChannelCases.SuiteGroup group = RandomChannelCases.SuiteGroup.M_ALL;
        String caseId = "M1_s2_base_a";
        boolean detailed = false;
        long directTimeoutMs = 120000L;
        long agTimeoutMs = 120000L;
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

        if (mode == Mode.GENERATE_CASES) {
            printModeHeader("GENERATE_CASES", group, null, false, directTimeoutMs, agTimeoutMs, agResetPolicy, repeatCount);
            RandomChannelCaseGenerator.generateAll();
            System.out.println("Random suites generated under " + RandomChannelCases.ROOT_DIR);
            return;
        }

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
                List<RandomChannelCase> selected = RandomChannelCases.loadCases(group);
                printModeHeader("SUMMARY_TABLE", group, null, false, directTimeoutMs, agTimeoutMs, agResetPolicy, repeatCount);
                printRunList(selected);
                List<SummaryRow> rows = runSummary(selected, directTimeoutMs, agTimeoutMs, repeatCount);
                printSummaryTable(rows);
                writeSummaryOutputs(rows);
                return;
            default:
                throw new IllegalStateException("Unsupported mode: " + mode);
        }
    }

    private static void printModeHeader(String mode, RandomChannelCases.SuiteGroup group, String caseId,
                                        boolean detailed, long directTimeoutMs, long agTimeoutMs,
                                        ResetPolicyType agResetPolicy, int repeatCount) {
        System.out.println("============================================================");
        System.out.println("Random Channel Executor");
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

    private static void printRunList(List<RandomChannelCase> selected) {
        System.out.println("Run list:");
        for (RandomChannelCase view : selected) {
            System.out.println("  - " + view.getSuiteId() + " / " + view.getCaseId()
                    + " [" + view.getSuitePurpose() + ", " + view.getTopologyKind()
                    + ", sigma=" + view.getAlphabetSize()
                    + ", profile=" + (view.getCaseProfile() == null || view.getCaseProfile().trim().isEmpty()
                    ? view.getVariantId() : view.getCaseProfile())
                    + ", mode=" + view.getModePattern()
                    + ", burst=" + view.getBurstLength()
                    + ", " + view.getStructureGroup() + ", " + view.getExpectedPreprocess() + "]"
                    + " -> " + view.getPropertyText());
        }
        System.out.println();
    }

    private static void requireSingleGroup(RandomChannelCases.SuiteGroup group) {
        if (group.isSyntheticGroup()) {
            throw new IllegalArgumentException("Single-case mode requires a concrete suite group.");
        }
    }

    private static RandomChannelCase findCase(RandomChannelCases.SuiteGroup group, String caseId) throws IOException {
        RandomChannelCase randomCase = RandomChannelCases.findCase(group, caseId);
        if (randomCase == null) {
            throw new IllegalArgumentException("Unknown random case for " + group + ": " + caseId);
        }
        return randomCase;
    }

    private static DirectRunResult runDirectOnly(RandomChannelCase view, long timeoutMs) throws Exception {
        DirectRunResult result = new DirectRunResult();
        result.suiteId = view.getSuiteId();
        result.caseId = view.getCaseId();
        result.family = view.getFamily();
        result.structureGroup = view.getStructureGroup();
        result.description = view.getDescription();
        result.verifyGoal = view.getPropertyText();
        result.m1Desc = view.getM1Desc();
        result.m2Desc = view.getM2Desc();
        result.preprocess = describePreprocess(view.newExperiment());
        result.timeoutMs = timeoutMs;

        long begin = System.currentTimeMillis();
        try {
            final Experiment directExp = view.newExperiment();
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

    private static AgRunResult runAgOnly(RandomChannelCase view, long timeoutMs, boolean detailed,
                                         ResetPolicyType agResetPolicy) throws Exception {
        AgRunResult result = new AgRunResult();
        result.suiteId = view.getSuiteId();
        result.caseId = view.getCaseId();
        result.family = view.getFamily();
        result.structureGroup = view.getStructureGroup();
        result.description = view.getDescription();
        result.verifyGoal = view.getPropertyText();
        result.m1Desc = view.getM1Desc();
        result.m2Desc = view.getM2Desc();
        result.timeoutMs = timeoutMs;

        Experiment summaryExp = overrideResetPolicy(view.newExperiment(), agResetPolicy);
        result.preprocess = describePreprocess(summaryExp);
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

    private static CompareResult runCompare(RandomChannelCase view, long directTimeoutMs,
                                            long agTimeoutMs, boolean detailed,
                                            ResetPolicyType agResetPolicy) throws Exception {
        CompareResult result = new CompareResult();
        result.suiteId = view.getSuiteId();
        result.caseId = view.getCaseId();
        result.family = view.getFamily();
        result.structureGroup = view.getStructureGroup();
        result.description = view.getDescription();
        result.verifyGoal = view.getPropertyText();
        result.m1Desc = view.getM1Desc();
        result.m2Desc = view.getM2Desc();
        result.directTimeoutMs = directTimeoutMs;
        result.agTimeoutMs = agTimeoutMs;

        Experiment summaryExp = overrideResetPolicy(view.newExperiment(), agResetPolicy);
        result.preprocess = describePreprocess(summaryExp);
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

            AgRunResult ag = runAgOnly(view, agTimeoutMs, detailed, agResetPolicy);
            result.agVerdict = ag.verdict == null ? "UNKNOWN" : ag.verdict.name();
            result.agTimeMs = ag.reportElapsedMs >= 0 ? ag.reportElapsedMs : ag.elapsedMs;
            result.agError = ag.error;
            result.cq1Fails = ag.cq1Fails;
            result.cq2Fails = ag.cq2Fails;
            result.secondary = ag.secondary;
            result.finalStates = ag.finalStates;
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
    private static List<SummaryRow> runSummary(List<RandomChannelCase> selected, long directTimeoutMs,
                                               long agTimeoutMs, int repeatCount) throws Exception {
        List<SummaryRow> rows = new ArrayList<SummaryRow>();
        for (RandomChannelCase view : selected) {
            rows.add(runSummaryRow(view, directTimeoutMs, agTimeoutMs, repeatCount));
        }
        return rows;
    }

    private static SummaryRow runSummaryRow(RandomChannelCase view, long directTimeoutMs,
                                            long agTimeoutMs, int repeatCount) throws Exception {
        SummaryRow row = new SummaryRow();
        row.suiteId = view.getSuiteId();
        row.caseId = view.getCaseId();
        row.family = view.getFamily();
        row.suitePurpose = view.getSuitePurpose();
        row.topologyKind = view.getTopologyKind();
        row.alphabetSize = view.getAlphabetSize();
        row.variantId = view.getVariantId();
        row.caseProfile = view.getCaseProfile();
        row.showcaseTarget = view.getShowcaseTarget();
        row.modePattern = view.getModePattern();
        row.modeCount = view.getModeCount();
        row.burstLength = view.getBurstLength();
        row.structureGroup = view.getStructureGroup();
        row.description = view.getDescription();
        row.verifyGoal = view.getPropertyText();
        row.preprocess = describePreprocess(view.newExperiment());

        row.direct = aggregateDirect(view, directTimeoutMs, repeatCount);
        row.staticAg = aggregateAg(view, agTimeoutMs, ResetPolicyType.STATIC_SIGMA, repeatCount);
        row.dynamicAg = aggregateAg(view, agTimeoutMs, ResetPolicyType.DYNAMIC_GAMMA, repeatCount);
        row.valid = resolveValidLabel(row.direct, row.staticAg, row.dynamicAg);
        return row;
    }

    private static DirectAggregate aggregateDirect(RandomChannelCase view, long timeoutMs, int repeatCount) throws Exception {
        DirectAggregate aggregate = new DirectAggregate();
        aggregate.timeoutMs = timeoutMs;
        aggregate.repeatCount = repeatCount;

        long totalMs = 0L;
        int successfulRuns = 0;
        String expectedVerdict = null;
        for (int i = 0; i < repeatCount; i++) {
            DirectRunResult run = runDirectOnly(view, timeoutMs);
            if (run.error != null) {
                aggregate.status = classifyErrorStatus(run.error);
                aggregate.error = run.error;
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

    private static AgAggregate aggregateAg(RandomChannelCase view, long timeoutMs,
                                           ResetPolicyType policy, int repeatCount) throws Exception {
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

    private static void printDirectSummary(DirectRunResult s) {
        System.out.println("------------------------------------------------------------");
        System.out.println("Random Direct Summary");
        System.out.println("Suite          : " + s.suiteId);
        System.out.println("Case ID        : " + s.caseId);
        System.out.println("Family         : " + s.family);
        System.out.println("Structure      : " + s.structureGroup);
        System.out.println("Property       : " + s.verifyGoal);
        System.out.println("M1             : " + s.m1Desc);
        System.out.println("M2             : " + s.m2Desc);
        System.out.println("Preprocess     : " + s.preprocess);
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
        System.out.println("suite       : " + summary.suiteId);
        System.out.println("caseId      : " + summary.caseId);
        System.out.println("family      : " + summary.family);
        System.out.println("structure   : " + summary.structureGroup);
        System.out.println("property    : " + summary.verifyGoal);
        System.out.println("sigma       : " + summary.targetSigma);
        System.out.println("resetPolicy : " + summary.resetPolicy);
        System.out.println("resetSigma  : " + summary.resetSigma);
        System.out.println("preprocess  : " + summary.preprocess);
        System.out.println("timeoutMs   : " + summary.timeoutMs);
        System.out.println("------------------------------------------------------------");
    }

    private static void printAgSummary(AgRunResult s) {
        System.out.println("------------------------------------------------------------");
        System.out.println("Random AG Summary");
        System.out.println("Suite          : " + s.suiteId);
        System.out.println("Case ID        : " + s.caseId);
        System.out.println("Family         : " + s.family);
        System.out.println("Structure      : " + s.structureGroup);
        System.out.println("Property       : " + s.verifyGoal);
        System.out.println("M1             : " + s.m1Desc);
        System.out.println("M2             : " + s.m2Desc);
        System.out.println("Target sigma   : " + s.targetSigma);
        System.out.println("Reset sigma    : " + s.resetSigma);
        System.out.println("Reset policy   : " + s.resetPolicy);
        System.out.println("Preprocess     : " + s.preprocess);
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
        System.out.println("Random Channel Compare Summary");
        System.out.println("Suite          : " + s.suiteId);
        System.out.println("Case ID        : " + s.caseId);
        System.out.println("Family         : " + s.family);
        System.out.println("Structure      : " + s.structureGroup);
        System.out.println("Property       : " + s.verifyGoal);
        System.out.println("M1             : " + s.m1Desc);
        System.out.println("M2             : " + s.m2Desc);
        System.out.println("Target sigma   : " + s.targetSigma);
        System.out.println("Reset sigma    : " + s.resetSigma);
        System.out.println("Reset policy   : " + s.resetPolicy);
        System.out.println("Preprocess     : " + s.preprocess);
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
        printSummaryTableSection("Main Performance Suites", filterMainPerformanceRows(rows));
        printSummaryTableSection("Nontrivial Learning Showcase", filterLearningRows(rows));
        printSummaryTableSection("Pressure Suites", filterRowsBySeries(rows, "P"));
        printSummaryTableSection("Sanity Suites", filterRowsBySeries(rows, "G"));
    }

    private static void writeSummaryOutputs(List<SummaryRow> rows) throws IOException {
        List<SummaryRow> mainRows = filterMainPerformanceRows(rows);
        List<SummaryRow> learningRows = filterLearningRows(rows);
        List<SummaryRow> pressureRows = filterRowsBySeries(rows, "P");
        List<SummaryRow> sanityRows = filterRowsBySeries(rows, "G");
        if (!mainRows.isEmpty()) {
            writeResultBundle(new File(RandomChannelCases.M_RESULT_DIR), mainRows,
                    "random_direct.csv", "random_ag_static.csv", "random_ag_dynamic.csv",
                    "random_mid_summary.csv", "random_mid_summary.md");
        }
        if (!learningRows.isEmpty()) {
            writeResultBundle(new File(RandomChannelCases.M_RESULT_DIR), learningRows,
                    "random_nontrivial_direct.csv", "random_nontrivial_ag_static.csv", "random_nontrivial_ag_dynamic.csv",
                    "random_nontrivial_summary.csv", "random_nontrivial_summary.md");
        }
        if (!sanityRows.isEmpty()) {
            writeResultBundle(new File(RandomChannelCases.G_RESULT_DIR), sanityRows,
                    "random_direct.csv", "random_ag_static.csv", "random_ag_dynamic.csv",
                    "random_sanity_summary.csv", "random_sanity_summary.md");
        }
        if (!pressureRows.isEmpty()) {
            writeResultBundle(new File(RandomChannelCases.P_RESULT_DIR), pressureRows,
                    "random_direct.csv", "random_ag_static.csv", "random_ag_dynamic.csv",
                    "random_perf_summary.csv", "random_perf_summary.md");
        }
    }

    private static void writeResultBundle(File root, List<SummaryRow> rows,
                                          String directName, String staticName, String dynamicName,
                                          String summaryCsvName, String summaryMdName) throws IOException {
        writeDirectCsv(new File(root, "direct/" + directName), rows);
        writeAgCsv(new File(root, "ag_static/" + staticName), rows, true);
        writeAgCsv(new File(root, "ag_dynamic/" + dynamicName), rows, false);
        writeSummaryCsv(new File(root, "summary/" + summaryCsvName), rows);
        writeSummaryMd(new File(root, "summary/" + summaryMdName), rows);
    }

    private static void printSummaryTableSection(String title, List<SummaryRow> rows) {
        if (rows.isEmpty()) {
            return;
        }
        System.out.println("## " + title);
        System.out.println("| Suite | Case ID | Sigma | Profile | ShowcaseTarget | Mode | Burst | Valid | Direct Tmean(ms) | Static Q/Sigma/R/T | Dynamic Q/Sigma/R/T | Static Speedup | Dynamic Speedup | Preprocess | Property |");
        System.out.println("|---|---|---:|---|---|---|---:|---|---:|---|---|---:|---:|---|---|");
        for (SummaryRow row : rows) {
            System.out.println("| " + row.suiteId
                    + " | " + row.caseId
                    + " | " + row.alphabetSize
                    + " | " + displayProfile(row)
                    + " | " + displayShowcaseTarget(row)
                    + " | " + row.modePattern
                    + " | " + row.burstLength
                    + " | " + row.valid
                    + " | " + formatDirectMetric(row.direct)
                    + " | " + formatAgMetric(row.staticAg)
                    + " | " + formatAgMetric(row.dynamicAg)
                    + " | " + formatSpeedup(row.direct, row.staticAg)
                    + " | " + formatSpeedup(row.direct, row.dynamicAg)
                    + " | " + escapeMd(row.preprocess)
                    + " | " + escapeMd(row.verifyGoal)
                    + " |");
        }
        System.out.println();
    }

    private static List<SummaryRow> filterRowsBySeries(List<SummaryRow> rows, String seriesPrefix) {
        List<SummaryRow> filtered = new ArrayList<SummaryRow>();
        for (SummaryRow row : rows) {
            if (row.suiteId != null && row.suiteId.startsWith(seriesPrefix)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static List<SummaryRow> filterMainPerformanceRows(List<SummaryRow> rows) {
        List<SummaryRow> filtered = new ArrayList<SummaryRow>();
        for (SummaryRow row : rows) {
            if (row.suiteId != null && row.suiteId.startsWith("M") && !row.suiteId.startsWith("M4")) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static List<SummaryRow> filterLearningRows(List<SummaryRow> rows) {
        List<SummaryRow> filtered = new ArrayList<SummaryRow>();
        for (SummaryRow row : rows) {
            if (row.suiteId != null && row.suiteId.startsWith("M4")) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static void writeDirectCsv(File file, List<SummaryRow> rows) throws IOException {
        ensureDir(file.getParentFile());
        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        try {
            writer.write("suiteId,caseId,family,suitePurpose,topologyKind,alphabetSize,caseProfile,showcaseTarget,variantId,modePattern,modeCount,burstLength,verdict,status,tMeanMs,error");
            writer.newLine();
            for (SummaryRow row : rows) {
                writer.write(csv(row.suiteId) + "," + csv(row.caseId) + "," + csv(row.family) + ","
                        + csv(row.suitePurpose) + "," + csv(row.topologyKind) + ","
                        + row.alphabetSize + "," + csv(row.caseProfile) + "," + csv(row.showcaseTarget) + "," + csv(row.variantId) + ","
                        + csv(row.modePattern) + "," + row.modeCount + "," + row.burstLength + ","
                        + csv(row.direct.verdict) + "," + csv(row.direct.status) + ","
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
            writer.write("suiteId,caseId,family,suitePurpose,topologyKind,alphabetSize,caseProfile,showcaseTarget,variantId,modePattern,modeCount,burstLength,policy,verdict,status,match,tMeanMs,q,sigma,r,error");
            writer.newLine();
            for (SummaryRow row : rows) {
                AgAggregate ag = isStatic ? row.staticAg : row.dynamicAg;
                writer.write(csv(row.suiteId) + "," + csv(row.caseId) + "," + csv(row.family) + ","
                        + csv(row.suitePurpose) + "," + csv(row.topologyKind) + ","
                        + row.alphabetSize + "," + csv(row.caseProfile) + "," + csv(row.showcaseTarget) + "," + csv(row.variantId) + ","
                        + csv(row.modePattern) + "," + row.modeCount + "," + row.burstLength + ","
                        + csv(ag.policy) + ","
                        + csv(ag.verdict) + "," + csv(ag.status) + "," + csv(resolveMatch(row.direct, ag)) + ","
                        + ag.tMeanMs + "," + ag.q + "," + ag.sigma + "," + ag.r + "," + csv(ag.error));
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
            writer.write("suiteId,caseId,family,suitePurpose,topologyKind,alphabetSize,caseProfile,showcaseTarget,variantId,modePattern,modeCount,burstLength,valid,directVerdict,directStatus,directTMeanMs,staticVerdict,staticStatus,staticMatch,staticTMeanMs,staticQ,staticSigma,staticR,staticSpeedup,dynamicVerdict,dynamicStatus,dynamicMatch,dynamicTMeanMs,dynamicQ,dynamicSigma,dynamicR,dynamicSpeedup,preprocess,property");
            writer.newLine();
            for (SummaryRow row : rows) {
                writer.write(csv(row.suiteId) + "," + csv(row.caseId) + "," + csv(row.family) + ","
                        + csv(row.suitePurpose) + "," + csv(row.topologyKind) + ","
                        + row.alphabetSize + "," + csv(row.caseProfile) + "," + csv(row.showcaseTarget) + "," + csv(row.variantId) + ","
                        + csv(row.modePattern) + "," + row.modeCount + "," + row.burstLength + ","
                        + csv(row.valid) + ","
                        + csv(row.direct.verdict) + "," + csv(row.direct.status) + "," + row.direct.tMeanMs + ","
                        + csv(row.staticAg.verdict) + "," + csv(row.staticAg.status) + "," + csv(resolveMatch(row.direct, row.staticAg)) + ","
                        + row.staticAg.tMeanMs + "," + row.staticAg.q + "," + row.staticAg.sigma + "," + row.staticAg.r + ","
                        + csv(formatSpeedup(row.direct, row.staticAg)) + ","
                        + csv(row.dynamicAg.verdict) + "," + csv(row.dynamicAg.status) + "," + csv(resolveMatch(row.direct, row.dynamicAg)) + ","
                        + row.dynamicAg.tMeanMs + "," + row.dynamicAg.q + "," + row.dynamicAg.sigma + "," + row.dynamicAg.r + ","
                        + csv(formatSpeedup(row.direct, row.dynamicAg)) + ","
                        + csv(row.preprocess) + "," + csv(row.verifyGoal));
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
            writer.write("# Random Channel Summary");
            writer.newLine();
            writer.newLine();
            writeSummaryMdSection(writer, "Main Performance Suites", filterMainPerformanceRows(rows));
            writeSummaryMdSection(writer, "Nontrivial Learning Showcase", filterLearningRows(rows));
            writeSummaryMdSection(writer, "Pressure Suites", filterRowsBySeries(rows, "P"));
            writeSummaryMdSection(writer, "Sanity Suites", filterRowsBySeries(rows, "G"));
        } finally {
            writer.close();
        }
    }

    private static void writeSummaryMdSection(BufferedWriter writer, String title, List<SummaryRow> rows) throws IOException {
        if (rows.isEmpty()) {
            return;
        }
        writer.write("## " + title);
        writer.newLine();
        writer.newLine();
        writer.write("| Suite | Case ID | Sigma | Profile | ShowcaseTarget | Mode | Burst | Valid | Direct Tmean(ms) | Static Q/Sigma/R/T | Dynamic Q/Sigma/R/T | Static Speedup | Dynamic Speedup | Preprocess | Property |");
        writer.newLine();
        writer.write("|---|---|---:|---|---|---|---:|---|---:|---|---|---:|---:|---|---|");
        writer.newLine();
        for (SummaryRow row : rows) {
            writer.write("| " + row.suiteId
                    + " | " + row.caseId
                    + " | " + row.alphabetSize
                    + " | " + displayProfile(row)
                    + " | " + escapeMd(displayShowcaseTarget(row))
                    + " | " + row.modePattern
                    + " | " + row.burstLength
                    + " | " + row.valid
                    + " | " + formatDirectMetric(row.direct)
                    + " | " + formatAgMetric(row.staticAg)
                    + " | " + formatAgMetric(row.dynamicAg)
                    + " | " + formatSpeedup(row.direct, row.staticAg)
                    + " | " + formatSpeedup(row.direct, row.dynamicAg)
                    + " | " + escapeMd(row.preprocess)
                    + " | " + escapeMd(row.verifyGoal)
                    + " |");
            writer.newLine();
        }
        writer.newLine();
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

    private static String formatDirectMetric(DirectAggregate aggregate) {
        if (aggregate == null) {
            return "-";
        }
        if (!"PASS".equals(aggregate.status)) {
            return aggregate.status;
        }
        return String.valueOf(aggregate.tMeanMs);
    }

    private static String formatAgMetric(AgAggregate aggregate) {
        if (aggregate == null) {
            return "ERR";
        }
        if (!"PASS".equals(aggregate.status)) {
            return aggregate.status;
        }
        return aggregate.q + " / " + aggregate.sigma + " / " + aggregate.r + " / " + aggregate.tMeanMs;
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

    private static String displayProfile(SummaryRow row) {
        if (row == null) {
            return "";
        }
        if (row.caseProfile != null && !row.caseProfile.trim().isEmpty()) {
            return row.caseProfile;
        }
        return row.variantId == null ? "" : row.variantId;
    }

    private static String displayShowcaseTarget(SummaryRow row) {
        if (row == null || row.showcaseTarget == null || row.showcaseTarget.trim().isEmpty()) {
            return "-";
        }
        return row.showcaseTarget;
    }

    private static String resolveValidLabel(DirectAggregate direct, AgAggregate staticAg, AgAggregate dynamicAg) {
        if (direct != null && "PASS".equals(direct.status)) {
            return toValidLabel(direct.verdict);
        }
        if (direct != null
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

    private static String summarizeError(Exception e) {
        return e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage());
    }

    private static String classifyErrorStatus(String error) {
        if (error != null && error.contains(Verifyta.VERIFYTA_TIMEOUT)) {
            return "TIMEOUT";
        }
        return "ERROR";
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

    private static Mode parseModeArg(String[] args, Mode fallback) {
        String raw = parseArg(args, "--mode=", null);
        if (raw == null) {
            return fallback;
        }
        return Mode.valueOf(raw.trim().toUpperCase().replace('-', '_'));
    }

    private static RandomChannelCases.SuiteGroup parseGroupArg(String[] args, RandomChannelCases.SuiteGroup fallback) {
        String raw = parseArg(args, "--group=", null);
        if (raw == null) {
            return fallback;
        }
        return RandomChannelCases.SuiteGroup.valueOf(raw.trim().toUpperCase());
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
            public Map<String, Boolean> getSyncSendMap() {
                return delegate.getSyncSendMap();
            }

            @Override
            public Set<String> getResetSigma() {
                return delegate.getResetSigma();
            }

            @Override
            public List<Template> getM1() {
                return delegate.getM1();
            }

            @Override
            public List<Template> getM2() throws IOException {
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
            public List<SequenceChecker> getSequenceChecker() {
                return delegate.getSequenceChecker();
            }

            @Override
            public boolean isPortActionMode() {
                return delegate.isPortActionMode();
            }

            @Override
            public Set<String> getTargetSigma() {
                return delegate.getTargetSigma();
            }

            @Override
            public Map<String, String> getM1RenameMap() {
                return delegate.getM1RenameMap();
            }

            @Override
            public Map<String, String> getM2RenameMap() {
                return delegate.getM2RenameMap();
            }

            @Override
            public PrimeSplitConfig getPrimeSplitConfig() {
                return delegate.getPrimeSplitConfig();
            }

            @Override
            public PortPreprocessConfig getPortPreprocessConfig() {
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
    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String escapeMd(String value) {
        if (value == null) {
            return "-";
        }
        return value.replace("|", "\\|");
    }

    private static void ensureDir(File dir) throws IOException {
        if (dir == null) {
            return;
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }
    }

    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private static class DirectRunResult {
        private String suiteId;
        private String caseId;
        private String family;
        private String structureGroup;
        private String description;
        private String verifyGoal;
        private String m1Desc;
        private String m2Desc;
        private String preprocess;
        private long timeoutMs;
        private long elapsedMs = -1L;
        private AgVerdict verdict;
        private String error;
    }

    private static class AgRunResult {
        private String suiteId;
        private String caseId;
        private String family;
        private String structureGroup;
        private String description;
        private String verifyGoal;
        private String m1Desc;
        private String m2Desc;
        private String preprocess;
        private String targetSigma;
        private String resetSigma;
        private String resetPolicy;
        private long timeoutMs;
        private long elapsedMs = -1L;
        private long reportElapsedMs = -1L;
        private AgVerdict verdict;
        private int cq1Fails;
        private int cq2Fails;
        private String secondary = "-";
        private int finalStates = -1;
        private String error;
    }

    private static class CompareResult {
        private String suiteId;
        private String caseId;
        private String family;
        private String structureGroup;
        private String description;
        private String verifyGoal;
        private String m1Desc;
        private String m2Desc;
        private String preprocess;
        private String targetSigma;
        private String resetSigma;
        private String resetPolicy;
        private long directTimeoutMs;
        private long agTimeoutMs;
        private String directTruth;
        private long directTimeMs = -1L;
        private String directError;
        private String agVerdict;
        private long agTimeMs = -1L;
        private String agError;
        private int cq1Fails;
        private int cq2Fails;
        private String secondary = "-";
        private int finalStates = -1;
        private boolean match;
        private long totalTimeMs = -1L;

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

    private static class DirectAggregate {
        private long timeoutMs;
        private int repeatCount;
        private String verdict;
        private String status = "UNKNOWN";
        private long tMeanMs = -1L;
        private String error;
    }

    private static class AgAggregate {
        private long timeoutMs;
        private int repeatCount;
        private String policy;
        private String verdict;
        private String status = "UNKNOWN";
        private long tMeanMs = -1L;
        private int q = -1;
        private int sigma = -1;
        private int r = -1;
        private String error;
    }

    private static class SummaryRow {
        private String suiteId;
        private String caseId;
        private String family;
        private String suitePurpose;
        private String topologyKind;
        private int alphabetSize;
        private String variantId;
        private String caseProfile;
        private String showcaseTarget;
        private String modePattern;
        private int modeCount;
        private int burstLength;
        private String structureGroup;
        private String description;
        private String verifyGoal;
        private String preprocess;
        private String valid;
        private DirectAggregate direct;
        private AgAggregate staticAg;
        private AgAggregate dynamicAg;
    }
}
