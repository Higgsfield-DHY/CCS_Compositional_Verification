package verification.experiment.pc_channel;

import verification.experiment.ChannelExecutor;
import verification.experiment.channel.ChannelExperimentSupport;

public final class PcChannelRunner {
    private PcChannelRunner() {
    }

    public static void main(String[] args) throws Exception {
        String presetCase = "PC_1";
        // presetCase = "PC_2";
        // presetCase = "PC_3";

        String caseArg = parseArg(args, "--case=");
        String selected = caseArg == null ? presetCase : caseArg;
        long timeoutMs = ChannelExperimentSupport.parseTimeoutMs(args);

        ChannelExecutor.main(new String[]{
                "--mode=compare-one",
                "--group=PC",
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
