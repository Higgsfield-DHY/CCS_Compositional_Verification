package verification.experiment.fischer_channel;

import verification.experiment.ChannelExecutor;
import verification.experiment.channel.ChannelExperimentSupport;

public final class FischerChannelRunner {
    private FischerChannelRunner() {
    }

    public static void main(String[] args) throws Exception {
        String presetCase = "Fischer2_1";
        // presetCase = "Fischer2_2";
        // presetCase = "Fischer3_1";
        // presetCase = "Fischer3_2";

        String caseArg = parseArg(args, "--case=");
        String selected = caseArg == null ? presetCase : caseArg;
        long timeoutMs = ChannelExperimentSupport.parseTimeoutMs(args);

        ChannelExecutor.main(new String[]{
                "--mode=compare-one",
                "--group=FISCHER",
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
