package verification.experiment.autosar1_channel.mutileM1;

import verification.experiment.autosar1_channel.Experiment1Channel;

/**
 * 对应原实验：autosar1.mutileM1.Experiment1_5
 * 验证性质：A[] not (runnable1.s2 and runnable2.s2)
 */
public class Experiment1_5 extends Experiment1Channel {
    @Override
    public String getStatement() {
        return "A[] not (runnable1.s2 and runnable2.s2)";
    }
}
