package verification.experiment.autosar1_channel;

import verification.experiment.autosar1.AutosarEx1Util;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 对应原实验基类：verification.experiment.autosar1.Experiment1
 * 用于 AUTOSAR-1（M1 为组合）的二元信道版镜像。
 */
public abstract class Experiment1Channel extends Autosar1ChannelExperiment {
    @Override
    public Map<String, Boolean> getSyncSendMap() {
        return mapOf("runnable1_start", true, "runnable2_start", true);
    }

    @Override
    public Set<String> getResetSigma() {
        Set<String> seed = new LinkedHashSet<String>();
        seed.add("runnable1_start");
        seed.add("runnable2_start");
        return toPortResetSigma(seed);
    }

    @Override
    public List<Template> getM1() {
        List<Template> list = new ArrayList<Template>();
        list.add(AutosarEx1Util.buildRunnable1());
        list.add(AutosarEx1Util.buildRunnable2());
        list.add(AutosarEx1Util.buildRunnable3());
        list.add(AutosarEx1Util.buildRunnable4());
        list.add(AutosarEx1Util.buildBuffer1());
        list.add(AutosarEx1Util.buildBuffer2());
        list.add(AutosarEx1Util.buildSchedule2());
        return list;
    }

    @Override
    public List<Template> getM2() {
        return Collections.singletonList(AutosarEx1Util.buildSchedule1());
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\autosar1_channel_ex2-source.xml";
    }

    @Override
    public Declaration getGlobalDeclaration() {
        return AutosarEx1Util.getGlobalDeclaration();
    }

    @Override
    public List<SequenceChecker> getSequenceChecker() {
        SequenceChecker plugin = new SequenceChecker(false);
        plugin.add("[startWith]:(" + portAction("runnable1_start", true) + "," + portAction("runnable1_start", true) + ")");
        plugin.add("[startWith]:(" + portAction("runnable1_start", true) + "," + portAction("runnable1_start", true)
                + "," + portAction("runnable2_start", true) + ")");
        plugin.add("[startWith]:(" + portAction("runnable1_start", true) + "," + portAction("runnable1_start", true)
                + "," + portAction("runnable1_start", true) + "," + portAction("runnable2_start", true) + ")");
        plugin.add("[startWith]:(" + portAction("runnable1_start", true) + "," + portAction("runnable2_start", true) + ")");
        return Collections.singletonList(plugin);
    }
}
