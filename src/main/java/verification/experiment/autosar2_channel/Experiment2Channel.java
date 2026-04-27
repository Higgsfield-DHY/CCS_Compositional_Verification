package verification.experiment.autosar2_channel;

import verification.experiment.autosar2.AutosarEx2Util;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Experiment2Channel extends Autosar2ChannelExperiment {
    @Override
    public Map<String, Boolean> getSyncSendMap() {
        Map<String, Boolean> syncSendMap = new LinkedHashMap<String, Boolean>();
        syncSendMap.put("runnable1_start", true);
        syncSendMap.put("task_start", true);
        return syncSendMap;
    }

    @Override
    public Set<String> getResetSigma() {
        Set<String> seed = new LinkedHashSet<String>();
        seed.add("runnable1_start");
        seed.add("task_start");
        return toPortResetSigma(seed);
    }

    @Override
    public List<Template> getM1() {
        List<Template> list = new ArrayList<Template>();
        list.add(AutosarEx2Util.buildRunnable1());
        list.add(AutosarEx2Util.buildRunnable2());
        list.add(AutosarEx2Util.buildRunnable3());
        list.add(AutosarEx2Util.buildRunnable4());
        list.add(AutosarEx2Util.buildRunnable5());
        list.add(AutosarEx2Util.buildRunnable6());
        list.add(AutosarEx2Util.buildBuffer1());
        list.add(AutosarEx2Util.buildBuffer2());
        list.add(AutosarEx2Util.buildBuffer3());
        list.add(AutosarEx2Util.buildBuffer4());
        list.add(AutosarEx2Util.buildBuffer5());
        list.add(AutosarEx2Util.buildRTE());
        list.add(AutosarEx2Util.buildTask());
        return list;
    }

    @Override
    public List<Template> getM2() {
        return Collections.singletonList(AutosarEx2Util.buildSchedule());
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\autosar2_channel-source.xml";
    }

    @Override
    public Declaration getGlobalDeclaration() {
        return AutosarEx2Util.getGlobalDeclaration();
    }

    @Override
    public List<SequenceChecker> getSequenceChecker() {
        SequenceChecker plugin = new SequenceChecker(false);
        plugin.add("[startWith]:(" + portAction("runnable1_start", true) + "," + portAction("task_start", true) + ")");
        plugin.add("[startWith]:(" + portAction("runnable1_start", true) + "," + portAction("runnable1_start", true) + ")");
        plugin.add("[startWith]:(" + portAction("runnable1_start", true) + "," + portAction("runnable1_start", true)
                + "," + portAction("task_start", true) + ")");
        plugin.add("[startWith]:(" + portAction("runnable1_start", true) + "," + portAction("runnable1_start", true)
                + "," + portAction("runnable1_start", true) + "," + portAction("task_start", true) + ")");
        return Collections.singletonList(plugin);
    }
}

