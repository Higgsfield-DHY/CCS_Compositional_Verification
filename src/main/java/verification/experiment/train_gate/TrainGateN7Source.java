package verification.experiment.train_gate;

import verification.experiment.train_gate_channel.TrainGateChannelExperiment;

/**
 * Adapted from the official UPPAAL Train-Gate tutorial/demo, 7 trains.
 */
public class TrainGateN7Source extends TrainGateChannelExperiment {
    @Override
    protected int getTrainCount() {
        return 7;
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\train_gate_n7_source.xml";
    }
}
