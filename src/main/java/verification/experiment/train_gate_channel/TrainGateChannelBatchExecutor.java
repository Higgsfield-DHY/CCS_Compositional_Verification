package verification.experiment.train_gate_channel;

import verification.experiment.ChannelExecutor;
import verification.experiment.channel.ChannelExperimentSupport;

public final class TrainGateChannelBatchExecutor {
    private TrainGateChannelBatchExecutor() {
    }

    public static void main(String[] args) throws Exception {
        long timeoutMs = ChannelExperimentSupport.parseTimeoutMs(args);
        ChannelExecutor.main(new String[]{
                "--mode=summary-table",
                "--group=TRAIN_GATE",
                "--direct-timeout-ms=" + timeoutMs,
                "--ag-timeout-ms=" + timeoutMs
        });
    }
}
