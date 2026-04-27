package verification.experiment.threea;

import verification.experiment.Experiment;
import verification.util.PortActionUtil;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ThreeARunner {
    private ThreeARunner() {
    }

    public static void main(String[] args) throws IOException {
        run();
    }

    public static void run() throws IOException {
        Experiment experiment = new ThreeAExperiment();
        System.out.println("Running 3a3a non-trivial case");
        System.out.println("Preprocess mode: BIDIRECTIONAL_DOMAIN_SPLIT");
        System.out.println("targetSigma(physical): " + experiment.getTargetSigma());
        System.out.println("targetSigma(logical): " + toLogicalSigmaView(experiment.getTargetSigma()));
        experiment.execute(true, false, false, 1);
    }

    private static Set<String> toLogicalSigmaView(Set<String> physicalSigma) {
        Set<String> logical = new LinkedHashSet<String>();
        for (String action : physicalSigma) {
            if (!PortActionUtil.isPortAction(action)) {
                logical.add(action);
                continue;
            }
            char suffix = action.charAt(action.length() - 1);
            String channel = PortActionUtil.channelOf(action);
            int idx = channel.indexOf('_');
            String logicalChannel = idx > 0 ? channel.substring(0, idx) : channel;
            logical.add(logicalChannel + suffix);
        }
        return logical;
    }
}
