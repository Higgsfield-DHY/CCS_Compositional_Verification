package verification.experiment.pc_channel;

import verification.experiment.pc.PcP1C3K2Source;

/**
 * Adapted from FM2026 RealCases/PC/P1_C3_K2.
 */
public class PC_1 extends PcP1C3K2Source {
    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\pc_channel_case1.xml";
    }
}
