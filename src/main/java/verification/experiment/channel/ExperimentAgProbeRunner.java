package verification.experiment.channel;

import verification.experiment.Experiment;
import verification.plugins.SequenceChecker;
import verification.report.AgRunReport;

import java.util.List;

public final class ExperimentAgProbeRunner {
    private ExperimentAgProbeRunner() {
    }

    public static void main(String[] args) throws Exception {
        String className = parseArg(args, "--class=");
        if (className == null || className.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing --class=<fqcn or experiment path>");
        }
        long timeoutMs = ChannelExperimentSupport.parseTimeoutMs(args);

        Experiment experiment = ChannelExperimentSupport.instantiateSourceExperiment(className);
        boolean sequenceCheck = hasSequenceChecker(experiment.getSequenceChecker());

        AgRunReport report = ChannelExperimentSupport.withVerifytaTimeout(timeoutMs,
                new ChannelExperimentSupport.CheckedSupplier<AgRunReport>() {
                    @Override
                    public AgRunReport get() throws Exception {
                        return experiment.executeWithReport(true, false, sequenceCheck, 1);
                    }
                });

        System.out.println("AG Probe Summary");
        System.out.println("Class       : " + className);
        System.out.println("Timeout(ms) : " + timeoutMs);
        System.out.println("Verdict     : " + report.getVerdict());
        System.out.println("CQ1/CQ2     : " + report.getCq1FailCount() + " / " + report.getCq2FailCount());
        System.out.println("States      : " + report.getFinalStateCount());
        System.out.println("Elapsed(ms) : " + report.getElapsedMs());
    }

    private static boolean hasSequenceChecker(List<SequenceChecker> sequenceCheckers) {
        return sequenceCheckers != null && !sequenceCheckers.isEmpty();
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
}
