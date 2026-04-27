package verification.experiment.autosar2_channel;

import verification.experiment.Experiment;
import verification.experiment.channel.ChannelPortMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class Autosar2ChannelExperiment extends Experiment {
    @Override
    public boolean isPortActionMode() {
        return true;
    }

    @Override
    public Set<String> getTargetSigma() {
        return ChannelPortMapper.inferTargetSigmaFromMap(getSyncSendMap());
    }

    protected Set<String> toPortResetSigma(Set<String> resetSeed) {
        return ChannelPortMapper.mapResetSeedToPort(resetSeed, getSyncSendMap());
    }

    protected static String portAction(String channel, boolean send) {
        return channel + (send ? "!" : "?");
    }

    protected static Map<String, Boolean> mapOf(String c1, boolean d1, String c2, boolean d2) {
        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
        map.put(c1, d1);
        map.put(c2, d2);
        return map;
    }
}

