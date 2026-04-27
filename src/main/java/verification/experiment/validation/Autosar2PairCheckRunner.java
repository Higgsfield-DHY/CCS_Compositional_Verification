package verification.experiment.validation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Autosar2PairCheckRunner {
    private static final String GROUP = "AUTOSAR2_PAIR";
    private static final int REPEAT = 3;

    public static void main(String[] args) {
        ValidationSupport.printRuntimeContext("Autosar2PairCheckRunner");
        ValidationSupport.cleanupStaleVerifytaProcesses();

        List<PairCase> pairs = buildPairs();
        boolean allPass = true;

        String format = "%-28s | %-17s | %-17s | %-15s | %-8s | %s%n";
        System.out.printf(format, "Case", "Direct(O/C)", "Verdict(O/C)", "CQ(O/C)", "Status", "Note");
        System.out.printf(format, "----------------------------", "-----------------", "-----------------",
                "---------------", "--------", "----");

        for (PairCase pair : pairs) {
            if (pair.exempt) {
                System.out.printf(format,
                        pair.caseId,
                        "-/-",
                        "-/-",
                        "-/-",
                        "EXEMPT",
                        "MutileClock strict mirror comparison is exempt");
                continue;
            }

            MajorityOutcome orig = runMajority(pair.caseId + "[orig]", pair.originalFactory);
            MajorityOutcome chan = runMajority(pair.caseId + "[chan]", pair.channelFactory);

            String status = "PASS";
            String note = "OK";
            if (orig.passRuns == 0 || chan.passRuns == 0) {
                status = "FAIL";
                note = "No successful run on one side";
            } else if (!orig.directMajority.equals(chan.directMajority)) {
                status = "FAIL";
                note = "DirectTruth mismatch";
            } else if (!orig.verdictMajority.equals(chan.verdictMajority)) {
                status = "FAIL";
                note = "AGVerdict mismatch";
            } else if (!orig.cqMajority.equals(chan.cqMajority)) {
                status = "FAIL";
                note = "CQ1/CQ2 majority mismatch";
            }

            String mergedNote = mergeNotes(note, orig.note, chan.note);
            if (!"PASS".equals(status)) {
                allPass = false;
            }

            System.out.printf(format,
                    pair.caseId,
                    orig.directMajority + "/" + chan.directMajority,
                    orig.verdictMajority + "/" + chan.verdictMajority,
                    orig.cqMajority + "/" + chan.cqMajority,
                    status,
                    mergedNote);
        }

        if (!allPass) {
            System.err.println("AUTOSAR2 strict mirror pair-check failed.");
            System.exit(1);
        }
        System.out.println("AUTOSAR2 strict mirror pair-check passed.");
    }

    private static MajorityOutcome runMajority(String runId, ValidationSupport.ExperimentFactory factory) {
        List<ValidationSupport.CaseOutcome> outcomes = new ArrayList<ValidationSupport.CaseOutcome>();
        for (int i = 1; i <= REPEAT; i++) {
            outcomes.add(ValidationSupport.runExperimentCase(
                    runId + "#" + i,
                    GROUP,
                    factory,
                    ValidationSupport.DEFAULT_TIMEOUT_MS));
        }

        MajorityOutcome majority = new MajorityOutcome();
        majority.directMajority = majorityOfDirect(outcomes);
        majority.verdictMajority = majorityOfVerdict(outcomes);
        majority.cqMajority = majorityOfCq(outcomes);

        int passCount = 0;
        StringBuilder note = new StringBuilder();
        Map<String, Integer> statusCount = new LinkedHashMap<String, Integer>();
        for (ValidationSupport.CaseOutcome outcome : outcomes) {
            String status = outcome.status.name();
            Integer old = statusCount.get(status);
            statusCount.put(status, old == null ? 1 : old + 1);
            if (outcome.status == ValidationSupport.RunStatus.PASS) {
                passCount++;
            } else if (outcome.note != null && outcome.note.length() > 0) {
                if (note.length() > 0) {
                    note.append(" ; ");
                }
                note.append(outcome.status.name()).append(':').append(trimNote(outcome.note));
            }
        }
        majority.passRuns = passCount;
        majority.note = "status=" + statusCount.toString() + (note.length() == 0 ? "" : " ; " + note.toString());
        return majority;
    }

    private static String majorityOfDirect(List<ValidationSupport.CaseOutcome> outcomes) {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        for (ValidationSupport.CaseOutcome outcome : outcomes) {
            String value = outcome.directTruth == null ? "UNKNOWN" : outcome.directTruth;
            increment(map, value);
        }
        return pickMajority(map);
    }

    private static String majorityOfVerdict(List<ValidationSupport.CaseOutcome> outcomes) {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        for (ValidationSupport.CaseOutcome outcome : outcomes) {
            String value = outcome.verdict == null ? "UNKNOWN" : outcome.verdict;
            increment(map, value);
        }
        return pickMajority(map);
    }

    private static String majorityOfCq(List<ValidationSupport.CaseOutcome> outcomes) {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        for (ValidationSupport.CaseOutcome outcome : outcomes) {
            String value = outcome.cq1 + "/" + outcome.cq2;
            increment(map, value);
        }
        return pickMajority(map);
    }

    private static void increment(Map<String, Integer> map, String key) {
        Integer old = map.get(key);
        map.put(key, old == null ? 1 : old + 1);
    }

    private static String pickMajority(Map<String, Integer> map) {
        String best = "UNKNOWN";
        int max = -1;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            int count = entry.getValue();
            if (count > max || (count == max && key.compareTo(best) < 0)) {
                max = count;
                best = key;
            }
        }
        return best;
    }

    private static String mergeNotes(String note, String origNote, String chanNote) {
        StringBuilder sb = new StringBuilder();
        sb.append(note);
        if (origNote != null && origNote.length() > 0) {
            sb.append(" | orig:").append(trimNote(origNote));
        }
        if (chanNote != null && chanNote.length() > 0) {
            sb.append(" | chan:").append(trimNote(chanNote));
        }
        return trimNote(sb.toString());
    }

    private static String trimNote(String text) {
        if (text == null) {
            return "-";
        }
        if (text.length() <= 180) {
            return text;
        }
        return text.substring(0, 177) + "...";
    }

    private static ValidationSupport.ExperimentFactory classFactory(final String className) {
        return new ValidationSupport.ExperimentFactory() {
            @Override
            public verification.experiment.Experiment create() throws Exception {
                return (verification.experiment.Experiment) Class.forName(className).newInstance();
            }
        };
    }

    private static List<PairCase> buildPairs() {
        List<PairCase> list = new ArrayList<PairCase>();
        list.add(new PairCase(
                "Experiment2_1",
                classFactory("verification.experiment.autosar2.Experiment2_1"),
                classFactory("verification.experiment.autosar2_channel.Experiment2_1"),
                false));
        list.add(new PairCase(
                "Experiment2_2",
                classFactory("verification.experiment.autosar2.Experiment2_2"),
                classFactory("verification.experiment.autosar2_channel.Experiment2_2"),
                false));
        list.add(new PairCase(
                "Experiment2_3",
                classFactory("verification.experiment.autosar2.Experiment2_3"),
                classFactory("verification.experiment.autosar2_channel.Experiment2_3"),
                false));
        list.add(new PairCase(
                "Experiment2_4",
                classFactory("verification.experiment.autosar2.Experiment2_4"),
                classFactory("verification.experiment.autosar2_channel.Experiment2_4"),
                false));
        return list;
    }

    private static class PairCase {
        private final String caseId;
        private final ValidationSupport.ExperimentFactory originalFactory;
        private final ValidationSupport.ExperimentFactory channelFactory;
        private final boolean exempt;

        private PairCase(String caseId,
                         ValidationSupport.ExperimentFactory originalFactory,
                         ValidationSupport.ExperimentFactory channelFactory,
                         boolean exempt) {
            this.caseId = caseId;
            this.originalFactory = originalFactory;
            this.channelFactory = channelFactory;
            this.exempt = exempt;
        }
    }

    private static class MajorityOutcome {
        private String directMajority;
        private String verdictMajority;
        private String cqMajority;
        private int passRuns;
        private String note;
    }
}
