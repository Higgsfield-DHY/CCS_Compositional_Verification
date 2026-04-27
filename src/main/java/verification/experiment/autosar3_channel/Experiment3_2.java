package verification.experiment.autosar3_channel;

import verification.experiment.autosar3.AutosarEx3Util;
import verification.uppaal.model.Template;

public class Experiment3_2 extends Autosar3BufferChannelExperiment {
    @Override
    protected String getReadChannel() {
        return "read2";
    }

    @Override
    protected String getWriteChannel() {
        return "write2";
    }

    @Override
    protected Template buildObservedBuffer() {
        return AutosarEx3Util.buildBuffer2();
    }

    @Override
    protected boolean includeObservedBuffer(Template template) {
        return !"buffer2".equals(template.getName());
    }

    @Override
    public String getStatement() {
        return "A[] buffer2.count >= 0";
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\autosar3_channel_ex2-source.xml";
    }
}
