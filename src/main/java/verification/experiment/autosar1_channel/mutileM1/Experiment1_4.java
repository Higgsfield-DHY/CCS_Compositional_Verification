package verification.experiment.autosar1_channel.mutileM1;

import verification.experiment.autosar1_channel.Experiment1Channel;

/**
 * 对应原实验：autosar1.mutileM1.Experiment1_4
 * 验证性质：A[] (buffer2.count <= buffer2.len)
 */
public class Experiment1_4 extends Experiment1Channel {
    @Override
    public String getStatement() {
        return "A[] (buffer2.count <= buffer2.len)";
    }
}
