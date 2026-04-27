package verification.experiment.channel;

import verification.experiment.Experiment;
import verification.experiment.autosar1_channel.Autosar1ChannelCase;
import verification.experiment.autosar1_channel.Autosar1ChannelCases;
import verification.experiment.autosar2_channel.Autosar2ChannelCase;
import verification.experiment.autosar2_channel.Autosar2ChannelCases;
import verification.experiment.autosar3_channel.Autosar3ChannelCase;
import verification.experiment.autosar3_channel.Autosar3ChannelCases;

import java.util.ArrayList;
import java.util.List;

public final class ChannelCaseMetricsDumpRunner {
    private ChannelCaseMetricsDumpRunner() {
    }

    public static void main(String[] args) throws Exception {
        List<CaseView> cases = new ArrayList<CaseView>();
        for (Autosar1ChannelCase c : Autosar1ChannelCases.allCases()) {
            cases.add(new CaseView("AUTOSAR-1 Channel", c.getCaseId(), c.getVerifyGoal(), c.getM1Desc(), c.getM2Desc(), c.newExperiment()));
        }
        for (Autosar2ChannelCase c : Autosar2ChannelCases.allCases()) {
            cases.add(new CaseView("AUTOSAR-2 Channel", c.getCaseId(), c.getVerifyGoal(), c.getM1Desc(), c.getM2Desc(), c.newExperiment()));
        }
        for (Autosar3ChannelCase c : Autosar3ChannelCases.allCases()) {
            cases.add(new CaseView("AUTOSAR-3 Channel", c.getCaseId(), c.getVerifyGoal(), c.getM1Desc(), c.getM2Desc(), c.newExperiment()));
        }

        for (CaseView view : cases) {
            ChannelCaseMetricsUtil.CaseMetrics metrics = ChannelCaseMetricsUtil.analyze(view.experiment);
            System.out.println("============================================================");
            System.out.println(view.groupName + " / " + view.caseId);
            System.out.println("property=" + view.property);
            System.out.println("m1=" + view.m1Desc);
            System.out.println("m2=" + view.m2Desc);
            System.out.println("m2LocalStateSpace=" + metrics.m2LocalStateSpace);
            System.out.println("m2ClockCount=" + metrics.m2ClockCount);
            System.out.println("m2AlphabetSize=" + metrics.m2AlphabetSize);
            System.out.println("m2Alphabet=" + metrics.m2Alphabet);
            for (ChannelCaseMetricsUtil.TemplateMetrics metric : metrics.templateMetrics) {
                System.out.println("  template=" + metric.templateName
                        + " localStateSpace=" + metric.localStateSpace
                        + " locations=" + metric.locationCount
                        + " clocks=" + metric.clockCount
                        + " ints=" + metric.intVariables
                        + " sync=" + metric.syncLabels);
            }
        }
    }

    private static final class CaseView {
        private final String groupName;
        private final String caseId;
        private final String property;
        private final String m1Desc;
        private final String m2Desc;
        private final Experiment experiment;

        private CaseView(String groupName, String caseId, String property,
                         String m1Desc, String m2Desc, Experiment experiment) {
            this.groupName = groupName;
            this.caseId = caseId;
            this.property = property;
            this.m1Desc = m1Desc;
            this.m2Desc = m2Desc;
            this.experiment = experiment;
        }
    }
}
