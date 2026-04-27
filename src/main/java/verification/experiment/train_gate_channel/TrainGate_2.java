package verification.experiment.train_gate_channel;

import verification.experiment.train_gate.TrainGateN8Source;

/**
 * Adapted from the official UPPAAL Train-Gate tutorial/demo, N=8 trains.
 */
public class TrainGate_2 extends TrainGateN8Source {
    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\train_gate_channel_case2.xml";
    }
}
