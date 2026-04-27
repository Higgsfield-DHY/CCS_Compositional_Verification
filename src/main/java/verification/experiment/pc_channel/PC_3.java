package verification.experiment.pc_channel;

import verification.experiment.pc.PcModelUtil;
import verification.uppaal.model.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapted from FM2026 RealCases/PC/P1_C3_K2 with all consumers internalized into M1
 * to avoid internal/external conflicts on get.
 */
public class PC_3 extends PcChannelExperiment {
    @Override
    public String getStatement() {
        return "A[] Buffer.count >= 0 && Buffer.count <= Buffer.len";
    }

    @Override
    public Map<String, Boolean> getSyncSendMap() {
        return mapOf("put", true);
    }

    @Override
    public List<Template> getM1() {
        List<Template> list = new ArrayList<Template>();
        list.add(PcModelUtil.buildBuffer());
        list.add(PcModelUtil.buildConsumer1());
        list.add(PcModelUtil.buildConsumer2());
        list.add(PcModelUtil.buildConsumer3());
        return list;
    }

    @Override
    public List<Template> getM2() {
        List<Template> list = new ArrayList<Template>();
        list.add(PcModelUtil.buildProducer1());
        return list;
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\pc_channel_case3.xml";
    }
}
