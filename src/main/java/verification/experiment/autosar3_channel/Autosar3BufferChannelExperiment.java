package verification.experiment.autosar3_channel;

import verification.experiment.autosar3.AutosarEx3Util;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Autosar3BufferChannelExperiment extends Autosar3ChannelExperiment {
    protected abstract String getReadChannel();

    protected abstract String getWriteChannel();

    protected abstract Template buildObservedBuffer();

    protected abstract boolean includeObservedBuffer(Template template);

    @Override
    public Map<String, Boolean> getSyncSendMap() {
        return mapOf(getReadChannel(), true, getWriteChannel(), true);
    }

    @Override
    public Set<String> getResetSigma() {
        Set<String> seed = new LinkedHashSet<String>();
        seed.add(getReadChannel());
        seed.add(getWriteChannel());
        return toPortResetSigma(seed);
    }

    @Override
    public List<Template> getM1() {
        return Collections.singletonList(buildObservedBuffer());
    }

    @Override
    public List<Template> getM2() throws IOException {
        List<Template> list = new ArrayList<Template>();
        list.add(AutosarEx3Util.buildRunnable1());
        list.add(AutosarEx3Util.buildRunnable2());
        list.add(AutosarEx3Util.buildRunnable3());
        list.add(AutosarEx3Util.buildRunnable4());
        list.add(AutosarEx3Util.buildRunnable5());
        list.add(AutosarEx3Util.buildRunnable6());
        list.add(AutosarEx3Util.buildRunnable7());
        addBufferIfNeeded(list, AutosarEx3Util.buildBuffer1());
        addBufferIfNeeded(list, AutosarEx3Util.buildBuffer2());
        addBufferIfNeeded(list, AutosarEx3Util.buildBuffer3());
        list.add(AutosarEx3Util.buildTask1());
        list.add(AutosarEx3Util.buildTask2());
        list.add(AutosarEx3Util.buildTask3());
        list.add(AutosarEx3Util.buildSchedule());
        return list;
    }

    private void addBufferIfNeeded(List<Template> list, Template template) {
        if (includeObservedBuffer(template)) {
            list.add(template);
        }
    }

    @Override
    public Declaration getGlobalDeclaration() {
        return AutosarEx3Util.buildGlobalDeclaration();
    }

    @Override
    public List<SequenceChecker> getSequenceChecker() {
        return null;
    }
}
