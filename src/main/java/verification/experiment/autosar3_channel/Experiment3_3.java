package verification.experiment.autosar3_channel;

import verification.experiment.autosar3.AutosarEx3Util;
import verification.uppaal.model.Template;

public class Experiment3_3 extends Autosar3BufferChannelExperiment {
    @Override
    protected String getReadChannel() {
        return "read3";
    }

    @Override
    protected String getWriteChannel() {
        return "write3";
    }

    @Override
    protected Template buildObservedBuffer() {
        return AutosarEx3Util.buildBuffer3();
    }

    @Override
    protected boolean includeObservedBuffer(Template template) {
        return !"buffer3".equals(template.getName());
    }

    @Override
    public String getStatement() {
        return "A[] buffer3.count >= 0";
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\autosar3_channel_ex3-source.xml";
    }
}
