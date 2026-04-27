package verification.experiment.autosar3_channel;

import verification.experiment.autosar3.AutosarEx3Util;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.util.VerificationUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Autosar3ComponentChannelExperiment extends Autosar3ChannelExperiment {
    @Override
    public Map<String, Boolean> getSyncSendMap() {
        Map<String, Boolean> sendMap = new LinkedHashMap<String, Boolean>();
        sendMap.put("runnable1_start", true);
        sendMap.put("runnable1_finish", true);
        sendMap.put("runnable2_start", true);
        sendMap.put("runnable2_finish", true);
        sendMap.put("task2_start", true);
        sendMap.put("task3_start", true);
        return sendMap;
    }

    @Override
    public Set<String> getResetSigma() {
        Set<String> seed = new LinkedHashSet<String>();
        seed.add("task3_start");
        return toPortResetSigma(seed);
    }

    @Override
    public List<Template> getM1() {
        List<Template> list = new ArrayList<Template>();
        list.add(AutosarEx3Util.buildRunnable1());
        list.add(AutosarEx3Util.buildRunnable2());
        list.add(AutosarEx3Util.buildRunnable3());
        list.add(AutosarEx3Util.buildRunnable4());
        list.add(AutosarEx3Util.buildRunnable5());
        list.add(AutosarEx3Util.buildRunnable6());
        list.add(AutosarEx3Util.buildRunnable7());
        list.add(AutosarEx3Util.buildBuffer1());
        list.add(AutosarEx3Util.buildBuffer2());
        list.add(AutosarEx3Util.buildBuffer3());
        list.add(AutosarEx3Util.buildTask2());
        list.add(AutosarEx3Util.buildTask3());
        return list;
    }

    @Override
    public List<Template> getM2() throws IOException {
        List<Template> list = new ArrayList<Template>();
        Template template = VerificationUtil.transToUppaal(AutosarEx3Util.buildComponent1_2_1());
        VerificationUtil.refine(template, getSyncSendMap(), true);
        list.add(template);
        return list;
    }

    @Override
    public Declaration getGlobalDeclaration() {
        return AutosarEx3Util.buildGlobalDeclaration();
    }

    @Override
    public List<SequenceChecker> getSequenceChecker() {
        SequenceChecker plugin = new SequenceChecker(false);
        plugin.add("[startWith]:(" + portAction("task2_start", true) + "," + portAction("runnable1_start", true) + ")");
        plugin.add("[startWith]:(" + portAction("runnable1_start", true) + "," + portAction("runnable1_finish", true) + ")");
        plugin.add("[startWith]:(" + portAction("runnable1_start", true) + ")");
        plugin.add("[startWith]:(" + portAction("runnable1_finish", true) + "," + portAction("runnable2_start", true) + ")");

        SequenceChecker plugin2 = new SequenceChecker(true);
        plugin2.add("[beforeAfter]:(" + portAction("runnable1_start", true) + ", " + portAction("runnable1_finish", true) + ")");
        plugin2.add("[beforeAfter]:(" + portAction("runnable2_start", true) + ", " + portAction("runnable2_finish", true) + ")");
        return Arrays.asList(plugin, plugin2);
    }
}
