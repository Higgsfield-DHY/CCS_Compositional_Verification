package verification.experiment.train_gate;

import verification.experiment.train_gate_channel.TrainGateChannelExperiment;

/**
 * Adapted from the official UPPAAL Train-Gate tutorial/demo, 9 trains.
 */
public class TrainGateN9Source extends TrainGateChannelExperiment {
    @Override
    protected int getTrainCount() {
        return 9;
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\train_gate_n9_source.xml";
    }
}
