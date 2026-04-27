package verification.experiment.autosar1_channel.mutileM1;

import verification.experiment.autosar1.AutosarEx1Util;
import verification.experiment.autosar1_channel.Experiment1Channel;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 对应原实验：autosar1.mutileM1.Experiment1_6
 * 验证性质：A[] not (runnable3.s2 and runnable4.s2)
 */
public class Experiment1_6 extends Experiment1Channel {
    @Override
    public String getStatement() {
        return "A[] not (runnable3.s2 and runnable4.s2)";
    }

    @Override
    public Map<String, Boolean> getSyncSendMap() {
        // Keep HashMap here to align with original autosar1 Experiment1_6 iteration behavior.
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        map.put("runnable3_start", true);
        map.put("runnable4_start", true);
        return map;
    }

    @Override
    public Set<String> getResetSigma() {
        Set<String> seed = new LinkedHashSet<String>();
        seed.add("runnable3_start");
        seed.add("runnable4_start");
        return toPortResetSigma(seed);
    }

    @Override
    public List<SequenceChecker> getSequenceChecker() {
        SequenceChecker plugin = new SequenceChecker(false);
        plugin.add("[startWith]:(" + portAction("runnable3_start", true) + "," + portAction("runnable4_start", true) + ")");
        plugin.add("[startWith]:(" + portAction("runnable3_start", true) + "," + portAction("runnable3_start", true) + ")");
        plugin.add("[startWith]:(" + portAction("runnable3_start", true) + "," + portAction("runnable3_start", true)
                + "," + portAction("runnable4_start", true) + ")");
        plugin.add("[startWith]:(" + portAction("runnable3_start", true) + "," + portAction("runnable3_start", true)
                + "," + portAction("runnable3_start", true) + "," + portAction("runnable4_start", true) + ")");
        return Collections.singletonList(plugin);
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
        list.add(AutosarEx1Util.buildSchedule1());
        return list;
    }

    @Override
    public List<Template> getM2() {
        return Collections.singletonList(AutosarEx1Util.buildSchedule2());
    }
}
