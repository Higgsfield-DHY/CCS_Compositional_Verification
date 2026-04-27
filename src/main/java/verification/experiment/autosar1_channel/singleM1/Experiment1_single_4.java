package verification.experiment.autosar1_channel.singleM1;

import verification.experiment.autosar1.AutosarEx1Util;
import verification.experiment.autosar1_channel.Autosar1ChannelExperiment;
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

/**
 * 对应原实验：autosar1.singleM1.Experiment1_single_4
 * 验证性质：A[] buffer2.count <= buffer2.len
 */
public class Experiment1_single_4 extends Autosar1ChannelExperiment {
    @Override
    public String getStatement() {
        return "A[] buffer2.count <= buffer2.len";
    }

    @Override
    public Map<String, Boolean> getSyncSendMap() {
        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
        map.put("read2", true);
        map.put("write2", true);
        return map;
    }

    @Override
    public Set<String> getResetSigma() {
        Set<String> seed = new LinkedHashSet<String>();
        seed.add("read2");
        seed.add("write2");
        return toPortResetSigma(seed);
    }

    @Override
    public List<Template> getM1() {
        return Collections.singletonList(AutosarEx1Util.buildBuffer2());
    }

    @Override
    public List<Template> getM2() {
        List<Template> list = new ArrayList<Template>();
        list.add(AutosarEx1Util.buildRunnable1());
        list.add(AutosarEx1Util.buildRunnable2());
        list.add(AutosarEx1Util.buildRunnable3());
        list.add(AutosarEx1Util.buildRunnable4());
        list.add(AutosarEx1Util.buildBuffer1());
        list.add(AutosarEx1Util.buildSchedule2());
        list.add(AutosarEx1Util.buildSchedule1());
        return list;
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
