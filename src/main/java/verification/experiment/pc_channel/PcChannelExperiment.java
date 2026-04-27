package verification.experiment.pc_channel;

import verification.experiment.Experiment;
import verification.experiment.channel.ChannelPortMapper;
import verification.experiment.pc.PcModelUtil;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class PcChannelExperiment extends Experiment {
    @Override
    public boolean isPortActionMode() {
        return true;
    }

    @Override
    public Set<String> getTargetSigma() {
        return ChannelPortMapper.inferTargetSigmaFromMap(getSyncSendMap());
    }

    @Override
    public Set<String> getResetSigma() {
        return ChannelPortMapper.mapResetSeedToPort(getSyncSendMap().keySet(), getSyncSendMap());
    }

    @Override
    public Declaration getGlobalDeclaration() {
        return PcModelUtil.buildGlobalDeclaration();
    }

    @Override
    public java.util.List<SequenceChecker> getSequenceChecker() {
        return null;
    }

    protected static String portAction(String channel, boolean send) {
        return channel + (send ? "!" : "?");
    }

    protected static Map<String, Boolean> mapOf(String c1, boolean d1) {
        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
        map.put(c1, d1);
        return map;
    }

    protected static Map<String, Boolean> mapOf(String c1, boolean d1, String c2, boolean d2) {
        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
        map.put(c1, d1);
        map.put(c2, d2);
        return map;
    }
}
