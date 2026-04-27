package verification.experiment.autosar2_channel;

import verification.experiment.channel.ChannelExperimentSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Autosar2ChannelBatchExecutor {
    public static void main(String[] args) throws IOException {
        boolean runAll = parseFlag(args, "--all");
        String caseArg = parseArg(args, "--case=");
        String verboseCaseArg = parseArg(args, "--verbose-case=");
        long timeoutMs = ChannelExperimentSupport.parseTimeoutMs(args);
        String verboseCaseId = verboseCaseArg == null ? null : verboseCaseArg.trim();
        if (verboseCaseId != null && verboseCaseId.isEmpty()) {
            verboseCaseId = null;
        }

        if (verboseCaseId != null && Autosar2ChannelCases.findById(verboseCaseId) == null) {
            throw new IllegalArgumentException("Unknown verbose case: " + verboseCaseId
                    + ". Available: " + Autosar2ChannelCases.availableCases());
        }

        List<Autosar2ChannelCase> selected = new ArrayList<Autosar2ChannelCase>();
        if (runAll) {
            selected.addAll(Autosar2ChannelCases.allCases());
        } else {
            String presetCase = "Experiment2_1";
            // presetCase = "Experiment2_2";
            // presetCase = "Experiment2_4";
            // presetCase = "MutileClockExperiment2_1";
            String selectedCaseId = caseArg == null ? presetCase : caseArg;
            Autosar2ChannelCase scenario = Autosar2ChannelCases.findById(selectedCaseId);
            if (scenario == null) {
                throw new IllegalArgumentException("Unknown case: " + selectedCaseId
                        + ". Available: " + Autosar2ChannelCases.availableCases());
            }
            if (verboseCaseId != null && !scenario.getCaseId().equalsIgnoreCase(verboseCaseId)) {
                throw new IllegalArgumentException("--verbose-case does not match selected case. selected="
                        + scenario.getCaseId() + ", verbose=" + verboseCaseId);
            }
            selected.add(scenario);
        }

        printBatchMode(verboseCaseId, timeoutMs);
        printCasePurpose(selected);
        List<Autosar2ChannelRunner.RunSummary> rows = new ArrayList<Autosar2ChannelRunner.RunSummary>();
        boolean allPass = true;
        for (Autosar2ChannelCase scenario : selected) {
            boolean detailed = verboseCaseId != null && scenario.getCaseId().equalsIgnoreCase(verboseCaseId);
            Autosar2ChannelRunner.RunSummary summary;
            if (detailed) {
                System.out.println();
                System.out.println("----- Detailed log start: " + scenario.getCaseId() + " -----");
                summary = Autosar2ChannelRunner.runCaseDetailed(scenario, timeoutMs);
                System.out.println("----- Detailed log end: " + scenario.getCaseId() + " -----");
                System.out.println();
            } else {
                summary = Autosar2ChannelRunner.runCase(scenario, timeoutMs);
            }
            rows.add(summary);
            if (!scenario.isBoundaryCase() && !"PASS".equals(summary.getStatus())) {
                allPass = false;
            }
        }

        printTable(rows);
        if (!allPass) {
            System.err.println("AUTOSAR-2 Channel batch validation failed.");
            System.exit(1);
        }
    }

    private static void printBatchMode(String verboseCaseId, long timeoutMs) {
        System.out.println("Batch log mode: SUMMARY");
        System.out.println("Timeout(ms)   : " + timeoutMs);
        if (verboseCaseId != null) {
            System.out.println("Verbose case  : " + verboseCaseId);
        }
        System.out.println();
    }

    private static void printCasePurpose(List<Autosar2ChannelCase> cases) {
        System.out.println("AUTOSAR-2 Channel run list:");
        for (Autosar2ChannelCase c : cases) {
            System.out.println("  - " + c.getCaseId() + " [" + c.getStructureGroup() + ", " + c.getPartitionNote() + "]"
                    + " -> " + c.getVerifyGoal());
        }
        System.out.println();
    }

    private static void printTable(List<Autosar2ChannelRunner.RunSummary> rows) {
        String format = "%-24s %-18s %-16s %-8s %-8s %-9s %-8s %-10s %-10s %s%n";
        System.out.printf(format,
                "Case", "Structure", "Partition", "Direct", "AG", "Status", "Direct(ms)", "AG(ms)", "Timeout", "Result");
        System.out.printf(format,
                "------------------------", "------------------", "----------------",
                "--------", "--------", "--------", "----------", "----------", "----------", "------");
        for (Autosar2ChannelRunner.RunSummary row : rows) {
            String result = row.getError() == null ? "OK" : row.getError();
            System.out.printf(format,
                    row.getCaseId(),
                    shorten(row.getStructureGroup(), 18),
                    shorten(row.getPartitionNote(), 16),
                    row.getDirectTruth(),
                    row.getAgVerdict(),
                    row.getStatus(),
                    row.getDirectTimeMs(),
                    row.getTimeMs(),
                    row.getTimeoutMs(),
                    shorten(result, 80));
        }
    }

    private static String shorten(String value, int maxLen) {
        if (value == null) {
            return "-";
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen - 3) + "...";
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

    private static boolean parseFlag(String[] args, String flag) {
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
}
