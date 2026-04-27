package verification.experiment.train_gate;

import verification.experiment.train_gate_channel.TrainGateChannelExperiment;

/**
 * Adapted from the official UPPAAL Train-Gate tutorial/demo, 8 trains.
 */
public class TrainGateN8Source extends TrainGateChannelExperiment {
    @Override
    protected int getTrainCount() {
        return 8;
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\train_gate_n8_source.xml";
    }
}
