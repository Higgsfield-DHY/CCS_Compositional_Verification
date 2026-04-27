package verification.experiment.train_gate_channel;

import verification.experiment.train_gate.TrainGateN7Source;

/**
 * Adapted from the official UPPAAL Train-Gate tutorial/demo, N=7 trains.
 */
public class TrainGate_1 extends TrainGateN7Source {
    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\train_gate_channel_case1.xml";
    }
}
