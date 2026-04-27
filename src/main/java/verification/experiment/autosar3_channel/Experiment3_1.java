package verification.experiment.autosar3_channel;

import verification.experiment.autosar3.AutosarEx3Util;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Experiment3_1 extends Autosar3ChannelExperiment {
    @Override
    public String getStatement() {
        return "A[] buffer1.count >= 0";
    }

    @Override
    public Map<String, Boolean> getSyncSendMap() {
        Map<String, Boolean> sendMap = new LinkedHashMap<String, Boolean>();
        sendMap.put("task1_start", true);
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
        list.add(AutosarEx3Util.buildTask1());
        list.add(AutosarEx3Util.buildTask2());
        list.add(AutosarEx3Util.buildTask3());
        return list;
    }

    @Override
    public List<Template> getM2() throws IOException {
        List<Template> list = new ArrayList<Template>();
        Template template = AutosarEx3Util.buildSchedule();
        list.add(template);
        return list;
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\autosar3_channel_ex1-source.xml";
    }

    @Override
    public Declaration getGlobalDeclaration() {
        return AutosarEx3Util.buildGlobalDeclaration();
    }

    @Override
    public List<SequenceChecker> getSequenceChecker() {
        SequenceChecker plugin = new SequenceChecker(true);
        plugin.add("[beforeAfter]:(" + portAction("task1_start", true) + "," + portAction("task3_start", true) + ")");
        return Collections.singletonList(plugin);
    }
}
