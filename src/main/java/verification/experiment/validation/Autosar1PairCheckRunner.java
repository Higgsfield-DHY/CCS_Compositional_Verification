package verification.experiment.validation;

import java.util.ArrayList;
import java.util.List;

public class Autosar1PairCheckRunner {
    private static final String GROUP = "AUTOSAR1_PAIR";

    public static void main(String[] args) {
        ValidationSupport.printRuntimeContext("Autosar1PairCheckRunner");
        ValidationSupport.cleanupStaleVerifytaProcesses();

        List<PairCase> pairs = buildPairs();
        boolean allPass = true;

        String format = "%-22s | %-12s | %-12s | %-11s | %-4s | %-6s | %s%n";
        System.out.printf(format,
                "PairCase", "Direct(O/C)", "Verdict(O/C)", "CQ(O/C)", "ISO", "Status", "Note");
        System.out.printf(format,
                "----------------------", "------------", "------------", "-----------", "----", "------", "----");

        for (PairCase pair : pairs) {
            ValidationSupport.CaseOutcome orig = ValidationSupport.runExperimentCase(
                    pair.caseId + "[orig]", GROUP, pair.originalFactory, ValidationSupport.DEFAULT_TIMEOUT_MS);
            ValidationSupport.CaseOutcome chan = ValidationSupport.runExperimentCase(
                    pair.caseId + "[chan]", GROUP, pair.channelFactory, ValidationSupport.DEFAULT_TIMEOUT_MS);

            String status;
            String note;
            String isoText = "NO";

            if (orig.status != ValidationSupport.RunStatus.PASS || chan.status != ValidationSupport.RunStatus.PASS) {
                status = "FAIL";
                note = "orig=" + orig.status + ":" + orig.note + " | chan=" + chan.status + ":" + chan.note;
                allPass = false;
            } else if (!orig.directTruth.equals(chan.directTruth)) {
                status = "FAIL";
                note = "DirectTruth mismatch";
                allPass = false;
            } else if (!orig.verdict.equals(chan.verdict)) {
                status = "FAIL";
                note = "AGVerdict mismatch";
                allPass = false;
            } else if (orig.cq1 != chan.cq1 || orig.cq2 != chan.cq2) {
                status = "FAIL";
                note = "CQ counts mismatch";
                allPass = false;
            } else {
                HypothesisIsoUtil.IsoResult isoResult = HypothesisIsoUtil.compare(
                        orig.report == null ? null : orig.report.getFinalHypothesis(),
                        chan.report == null ? null : chan.report.getFinalHypothesis());
                isoText = isoResult.isomorphic ? "YES" : "NO";
                if (!isoResult.isomorphic) {
                    status = "FAIL";
                    note = isoResult.message;
                    allPass = false;
                } else {
                    status = "PASS";
                    note = "OK";
                }
            }

            System.out.printf(format,
                    pair.caseId,
                    orig.directTruth + "/" + chan.directTruth,
                    orig.verdict + "/" + chan.verdict,
                    orig.cq1 + "/" + orig.cq2 + " vs " + chan.cq1 + "/" + chan.cq2,
                    isoText,
                    status,
                    note);
        }

        if (!allPass) {
            System.err.println("AUTOSAR1 对标核验失败。");
            System.exit(1);
        }
        System.out.println("AUTOSAR1 对标核验通过。");
    }

    private static List<PairCase> buildPairs() {
        List<PairCase> list = new ArrayList<PairCase>();
        list.add(new PairCase("Experiment1_single_1",
                classFactory("verification.experiment.autosar1.singleM1.Experiment1_single_1"),
                classFactory("verification.experiment.autosar1_channel.singleM1.Experiment1_single_1")));
        list.add(new PairCase("Experiment1_single_2",
                classFactory("verification.experiment.autosar1.singleM1.Experiment1_single_2"),
                classFactory("verification.experiment.autosar1_channel.singleM1.Experiment1_single_2")));
        list.add(new PairCase("Experiment1_single_3",
                classFactory("verification.experiment.autosar1.singleM1.Experiment1_single_3"),
                classFactory("verification.experiment.autosar1_channel.singleM1.Experiment1_single_3")));
        list.add(new PairCase("Experiment1_single_4",
                classFactory("verification.experiment.autosar1.singleM1.Experiment1_single_4"),
                classFactory("verification.experiment.autosar1_channel.singleM1.Experiment1_single_4")));

        list.add(new PairCase("Experiment1_1",
                classFactory("verification.experiment.autosar1.mutileM1.Experiment1_1"),
                classFactory("verification.experiment.autosar1_channel.mutileM1.Experiment1_1")));
        list.add(new PairCase("Experiment1_2",
                classFactory("verification.experiment.autosar1.mutileM1.Experiment1_2"),
                classFactory("verification.experiment.autosar1_channel.mutileM1.Experiment1_2")));
        list.add(new PairCase("Experiment1_3",
                classFactory("verification.experiment.autosar1.mutileM1.Experiment1_3"),
                classFactory("verification.experiment.autosar1_channel.mutileM1.Experiment1_3")));
        list.add(new PairCase("Experiment1_4",
                classFactory("verification.experiment.autosar1.mutileM1.Experiment1_4"),
                classFactory("verification.experiment.autosar1_channel.mutileM1.Experiment1_4")));
        list.add(new PairCase("Experiment1_5",
                classFactory("verification.experiment.autosar1.mutileM1.Experiment1_5"),
                classFactory("verification.experiment.autosar1_channel.mutileM1.Experiment1_5")));
        list.add(new PairCase("Experiment1_6",
                classFactory("verification.experiment.autosar1.mutileM1.Experiment1_6"),
                classFactory("verification.experiment.autosar1_channel.mutileM1.Experiment1_6")));
        return list;
    }

    private static ValidationSupport.ExperimentFactory classFactory(String className) {
        return new ValidationSupport.ExperimentFactory() {
            @Override
            public verification.experiment.Experiment create() throws Exception {
                return (verification.experiment.Experiment) Class.forName(className).newInstance();
            }
        };
    }

    private static class PairCase {
        private final String caseId;
        private final ValidationSupport.ExperimentFactory originalFactory;
        private final ValidationSupport.ExperimentFactory channelFactory;

        private PairCase(String caseId,
                         ValidationSupport.ExperimentFactory originalFactory,
                         ValidationSupport.ExperimentFactory channelFactory) {
            this.caseId = caseId;
            this.originalFactory = originalFactory;
            this.channelFactory = channelFactory;
        }
    }
}
