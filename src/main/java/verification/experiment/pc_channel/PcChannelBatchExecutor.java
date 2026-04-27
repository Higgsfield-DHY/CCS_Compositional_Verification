package verification.experiment.pc_channel;

import verification.experiment.ChannelExecutor;
import verification.experiment.channel.ChannelExperimentSupport;

public final class PcChannelBatchExecutor {
    private PcChannelBatchExecutor() {
    }

    public static void main(String[] args) throws Exception {
        long timeoutMs = ChannelExperimentSupport.parseTimeoutMs(args);
        ChannelExecutor.main(new String[]{
                "--mode=summary-table",
                "--group=PC",
                "--direct-timeout-ms=" + timeoutMs,
                "--ag-timeout-ms=" + timeoutMs
        });
    }
}
