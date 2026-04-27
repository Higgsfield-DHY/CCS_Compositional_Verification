package verification.experiment.train_gate_channel;

import verification.experiment.ChannelExecutor;
import verification.experiment.channel.ChannelExperimentSupport;

public final class TrainGateChannelRunner {
    private TrainGateChannelRunner() {
    }

    public static void main(String[] args) throws Exception {
        String presetCase = "TrainGate_1";
        // presetCase = "TrainGate_2";
        // presetCase = "TrainGate_3";

        String caseArg = parseArg(args, "--case=");
        String selected = caseArg == null ? presetCase : caseArg;
        long timeoutMs = ChannelExperimentSupport.parseTimeoutMs(args);

        ChannelExecutor.main(new String[]{
                "--mode=compare-one",
                "--group=TRAIN_GATE",
                "--case=" + selected,
                "--detailed",
                "--direct-timeout-ms=" + timeoutMs,
                "--ag-timeout-ms=" + timeoutMs
        });
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
