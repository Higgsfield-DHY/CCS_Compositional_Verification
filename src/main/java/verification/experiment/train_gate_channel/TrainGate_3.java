package verification.experiment.train_gate_channel;

import verification.experiment.train_gate.TrainGateN9Source;

/**
 * Adapted from the official UPPAAL Train-Gate tutorial/demo, N=9 trains.
 */
public class TrainGate_3 extends TrainGateN9Source {
    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\train_gate_channel_case3.xml";
    }
}
