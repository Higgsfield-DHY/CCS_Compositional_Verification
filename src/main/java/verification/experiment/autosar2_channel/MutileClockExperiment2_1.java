package verification.experiment.autosar2_channel;

import verification.experiment.autosar2.AutosarEx2Util;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.util.PortPreprocessConfig;
import verification.util.PortSplitMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MutileClockExperiment2_1 extends Autosar2ChannelExperiment {
    @Override
    public String getStatement() {
        return "A[] buffer2.count >= 0";
    }

    @Override
    public Map<String, Boolean> getSyncSendMap() {
        Map<String, Boolean> syncSendMap = new LinkedHashMap<String, Boolean>();
        syncSendMap.put("runnable1_start", true);
        syncSendMap.put("runnable5_start", true);
        syncSendMap.put("runnable6_start", true);
        syncSendMap.put("runnable3_start", false);
        syncSendMap.put("read2", true);
        syncSendMap.put("runnable4_start", true);
        return syncSendMap;
    }

    @Override
    public Set<String> getTargetSigma() {
        Set<String> sigma = new LinkedHashSet<String>();
        sigma.add("runnable1_start!");
        sigma.add("runnable5_start!");
        sigma.add("runnable6_start!");
        sigma.add("runnable3_start?");
        sigma.add("read2_m2_to_m1!");
        sigma.add("runnable4_start!");
        return sigma;
    }

    @Override
    public Set<String> getResetSigma() {
        Set<String> seed = new LinkedHashSet<String>();
        seed.add("runnable1_start");
        seed.add("runnable5_start");
        seed.add("runnable3_start");
        return toPortResetSigma(seed);
    }

    @Override
    public List<Template> getM1() {
        List<Template> list = new ArrayList<Template>();
        list.add(AutosarEx2Util.buildRunnable1());
        list.add(AutosarEx2Util.buildRunnable2());
        list.add(AutosarEx2Util.buildRunnable4());
        list.add(AutosarEx2Util.buildRunnable5());
        list.add(AutosarEx2Util.buildRunnable6());
        list.add(AutosarEx2Util.buildBuffer1());
        list.add(AutosarEx2Util.buildBuffer2());
        list.add(AutosarEx2Util.buildBuffer3());
        list.add(AutosarEx2Util.buildBuffer4());
        list.add(AutosarEx2Util.buildBuffer5());
        list.add(AutosarEx2Util.buildRTE());
        return list;
    }

    @Override
    public List<Template> getM2() {
        List<Template> list = new ArrayList<Template>();
        list.add(AutosarEx2Util.buildTask());
        list.add(AutosarEx2Util.buildSchedule());
        list.add(AutosarEx2Util.buildRunnable3());
        return list;
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\autosar2_channel_multi-source.xml";
    }

    @Override
    public Declaration getGlobalDeclaration() {
        return AutosarEx2Util.getGlobalDeclaration();
    }

    @Override
    public List<SequenceChecker> getSequenceChecker() {
        return null;
    }

    @Override
    public PortPreprocessConfig getPortPreprocessConfig() {
        return PortPreprocessConfig.of(
                PortSplitMode.BIDIRECTIONAL_DOMAIN_SPLIT,
                Collections.singleton("read2"),
                true);
    }
}
