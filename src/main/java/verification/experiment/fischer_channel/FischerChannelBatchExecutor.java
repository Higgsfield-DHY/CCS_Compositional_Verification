package verification.experiment.fischer_channel;

import verification.experiment.ChannelExecutor;
import verification.experiment.channel.ChannelExperimentSupport;

public final class FischerChannelBatchExecutor {
    private FischerChannelBatchExecutor() {
    }

    public static void main(String[] args) throws Exception {
        long timeoutMs = ChannelExperimentSupport.parseTimeoutMs(args);
        ChannelExecutor.main(new String[]{
                "--mode=summary-table",
                "--group=FISCHER",
                "--direct-timeout-ms=" + timeoutMs,
                "--ag-timeout-ms=" + timeoutMs
        });
    }
}
