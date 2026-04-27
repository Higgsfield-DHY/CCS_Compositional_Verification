package verification.experiment.autosar2_channel;

import verification.experiment.autosar2.AutosarEx2Util;
import verification.uppaal.model.Template;

import java.util.ArrayList;
import java.util.List;

public class Experiment2_4 extends Autosar2BufferChannelExperiment {
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
        return AutosarEx2Util.buildBuffer2();
    }

    @Override
    protected boolean includeObservedBuffer(Template template) {
        return !"buffer2".equals(template.getName());
    }

    @Override
    public List<Template> getM2() {
        List<Template> list = new ArrayList<Template>();
        list.add(AutosarEx2Util.buildRunnable1());
        list.add(AutosarEx2Util.buildRunnable2());
        list.add(AutosarEx2Util.buildRunnable3());
        list.add(AutosarEx2Util.buildBuffer1());
        list.add(AutosarEx2Util.buildBuffer4());
        list.add(AutosarEx2Util.buildRTE());
        list.add(AutosarEx2Util.buildSchedule());
        return list;
    }

    @Override
    public String getStatement() {
        return "A[] buffer2.count <= buffer2.len";
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\autosar2_channel_ex4-source.xml";
    }
}
