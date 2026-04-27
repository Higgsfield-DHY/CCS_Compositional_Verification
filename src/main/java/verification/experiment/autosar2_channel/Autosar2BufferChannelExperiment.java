package verification.experiment.autosar2_channel;

import verification.experiment.autosar2.AutosarEx2Util;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Autosar2BufferChannelExperiment extends Autosar2ChannelExperiment {
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
    public List<Template> getM2() {
        List<Template> list = new ArrayList<Template>();
        list.add(AutosarEx2Util.buildRunnable1());
        list.add(AutosarEx2Util.buildRunnable2());
        list.add(AutosarEx2Util.buildRunnable3());
        list.add(AutosarEx2Util.buildRunnable4());
        list.add(AutosarEx2Util.buildRunnable5());
        list.add(AutosarEx2Util.buildRunnable6());
        addBufferIfNeeded(list, AutosarEx2Util.buildBuffer1());
        addBufferIfNeeded(list, AutosarEx2Util.buildBuffer2());
        addBufferIfNeeded(list, AutosarEx2Util.buildBuffer3());
        addBufferIfNeeded(list, AutosarEx2Util.buildBuffer4());
        addBufferIfNeeded(list, AutosarEx2Util.buildBuffer5());
        list.add(AutosarEx2Util.buildRTE());
        list.add(AutosarEx2Util.buildTask());
        list.add(AutosarEx2Util.buildSchedule());
        return list;
    }

    private void addBufferIfNeeded(List<Template> list, Template template) {
        if (includeObservedBuffer(template)) {
            list.add(template);
        }
    }

    @Override
    public Declaration getGlobalDeclaration() {
        return AutosarEx2Util.getGlobalDeclaration();
    }

    @Override
    public List<SequenceChecker> getSequenceChecker() {
        return null;
    }
}
