package verification.experiment.validation;

import java.util.ArrayList;
import java.util.List;

public class FullRegressionRunner {
    private static final String GROUP_H16 = "H16";
    private static final String GROUP_A1_ORIG = "AUTOSAR1_ORIG";
    private static final String GROUP_A1_CHAN = "AUTOSAR1_CHANNEL";
    private static final String GROUP_A2_ORIG = "AUTOSAR2_ORIG";
    private static final String GROUP_A3_ORIG = "AUTOSAR3_ORIG";

    public static void main(String[] args) {
        boolean skipKnownLong = hasFlag(args, "--skip-known-long");

        ValidationSupport.printRuntimeContext("FullRegressionRunner");
        ValidationSupport.cleanupStaleVerifytaProcesses();

        List<ValidationSupport.CaseOutcome> rows = new ArrayList<ValidationSupport.CaseOutcome>();

        rows.add(ValidationSupport.runH16BatchCase(ValidationSupport.DEFAULT_TIMEOUT_MS));

        rows.add(runCase("Experiment1_single_1", GROUP_A1_ORIG,
                "verification.experiment.autosar1.singleM1.Experiment1_single_1", false));
        rows.add(runCase("Experiment1_single_2", GROUP_A1_ORIG,
                "verification.experiment.autosar1.singleM1.Experiment1_single_2", false));
        rows.add(runCase("Experiment1_single_3", GROUP_A1_ORIG,
                "verification.experiment.autosar1.singleM1.Experiment1_single_3", false));
        rows.add(runCase("Experiment1_single_4", GROUP_A1_ORIG,
                "verification.experiment.autosar1.singleM1.Experiment1_single_4", false));
        rows.add(runCase("Experiment1_1", GROUP_A1_ORIG,
                "verification.experiment.autosar1.mutileM1.Experiment1_1", false));
        rows.add(runCase("Experiment1_2", GROUP_A1_ORIG,
                "verification.experiment.autosar1.mutileM1.Experiment1_2", false));
        rows.add(runCase("Experiment1_3", GROUP_A1_ORIG,
                "verification.experiment.autosar1.mutileM1.Experiment1_3", false));
        rows.add(runCase("Experiment1_4", GROUP_A1_ORIG,
                "verification.experiment.autosar1.mutileM1.Experiment1_4", false));
        rows.add(runCase("Experiment1_5", GROUP_A1_ORIG,
                "verification.experiment.autosar1.mutileM1.Experiment1_5", false));
        rows.add(runCase("Experiment1_6", GROUP_A1_ORIG,
                "verification.experiment.autosar1.mutileM1.Experiment1_6", false));

        rows.add(runCase("Experiment1_single_1", GROUP_A1_CHAN,
                "verification.experiment.autosar1_channel.singleM1.Experiment1_single_1", false));
        rows.add(runCase("Experiment1_single_2", GROUP_A1_CHAN,
                "verification.experiment.autosar1_channel.singleM1.Experiment1_single_2", false));
        rows.add(runCase("Experiment1_single_3", GROUP_A1_CHAN,
                "verification.experiment.autosar1_channel.singleM1.Experiment1_single_3", false));
        rows.add(runCase("Experiment1_single_4", GROUP_A1_CHAN,
                "verification.experiment.autosar1_channel.singleM1.Experiment1_single_4", false));
        rows.add(runCase("Experiment1_1", GROUP_A1_CHAN,
                "verification.experiment.autosar1_channel.mutileM1.Experiment1_1", false));
        rows.add(runCase("Experiment1_2", GROUP_A1_CHAN,
                "verification.experiment.autosar1_channel.mutileM1.Experiment1_2", false));
        rows.add(runCase("Experiment1_3", GROUP_A1_CHAN,
                "verification.experiment.autosar1_channel.mutileM1.Experiment1_3", false));
        rows.add(runCase("Experiment1_4", GROUP_A1_CHAN,
                "verification.experiment.autosar1_channel.mutileM1.Experiment1_4", false));
        rows.add(runCase("Experiment1_5", GROUP_A1_CHAN,
                "verification.experiment.autosar1_channel.mutileM1.Experiment1_5", false));
        rows.add(runCase("Experiment1_6", GROUP_A1_CHAN,
                "verification.experiment.autosar1_channel.mutileM1.Experiment1_6", false));

        rows.add(runCase("Experiment2_1", GROUP_A2_ORIG,
                "verification.experiment.autosar2.Experiment2_1", false));
        rows.add(runCase("Experiment2_2", GROUP_A2_ORIG,
                "verification.experiment.autosar2.Experiment2_2", false));
        rows.add(runCase("Experiment2_3", GROUP_A2_ORIG,
                "verification.experiment.autosar2.Experiment2_3", false));
        rows.add(runCase("Experiment2_4", GROUP_A2_ORIG,
                "verification.experiment.autosar2.Experiment2_4", false));

        rows.add(runCase("Experiment3_1", GROUP_A3_ORIG,
                "verification.experiment.autosar3.Experiment3_1", false));
        rows.add(runCase("Experiment3_2", GROUP_A3_ORIG,
                "verification.experiment.autosar3.Experiment3_2", false));
        rows.add(runCase("Experiment3_3", GROUP_A3_ORIG,
                "verification.experiment.autosar3.Experiment3_3", false));

        printRows(rows);

        boolean failed = false;
        for (ValidationSupport.CaseOutcome row : rows) {
            if (row.status == ValidationSupport.RunStatus.FAIL || row.status == ValidationSupport.RunStatus.ERROR) {
                failed = true;
                break;
            }
        }
        if (failed) {
            System.err.println("扩展回归存在失败项，请查看上表。\n");
            System.exit(1);
        }
        System.out.println("扩展回归通过（KNOWN_LONG 与 SKIPPED 已按策略单独统计）。");
    }

    private static boolean hasFlag(String[] args, String flag) {
        if (args == null) {
            return false;
        }
        for (String arg : args) {
            if (flag.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    private static ValidationSupport.CaseOutcome runCase(String caseId,
                                                         String group,
                                                         String className,
                                                         boolean knownLongCase) {
        ValidationSupport.ExperimentFactory factory = new ValidationSupport.ExperimentFactory() {
            @Override
            public verification.experiment.Experiment create() throws Exception {
                return (verification.experiment.Experiment) Class.forName(className).newInstance();
            }
        };

        long timeout = knownLongCase ? ValidationSupport.KNOWN_LONG_TIMEOUT_MS : ValidationSupport.DEFAULT_TIMEOUT_MS;
        ValidationSupport.CaseOutcome outcome = ValidationSupport.runExperimentCase(caseId, group, factory, timeout);
        if (knownLongCase && ValidationSupport.isVerifytaTimeout(outcome)) {
            outcome.status = ValidationSupport.RunStatus.KNOWN_LONG;
            outcome.note = "超过30分钟，按已知长例统计";
        }
        return outcome;
    }

    private static void printRows(List<ValidationSupport.CaseOutcome> rows) {
        String format = "%-26s | %-16s | %-8s | %-10s | %-4s | %-4s | %-6s | %-9s | %s%n";
        System.out.printf(format,
                "Case", "Group", "Verdict", "Status", "CQ1", "CQ2", "States", "Time(ms)", "Note");
        System.out.printf(format,
                "--------------------------", "----------------", "--------", "----------", "----", "----",
                "------", "---------", "----");
        for (ValidationSupport.CaseOutcome row : rows) {
            System.out.printf(format,
                    row.caseId,
                    row.group,
                    row.verdict,
                    row.status,
                    row.cq1,
                    row.cq2,
                    row.states,
                    row.elapsedMs,
                    row.note == null ? "-" : row.note.replace('\n', ' '));
        }
    }
}
