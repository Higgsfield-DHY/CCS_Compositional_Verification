package verification.experiment.fischer_channel;

import verification.experiment.Experiment;
import verification.experiment.channel.ChannelPortMapper;
import verification.experiment.fischer.FischerModelUtil;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class FischerChannelExperiment extends Experiment {
    protected abstract int getProcessCount();

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
        return FischerModelUtil.buildGlobalDeclaration(getProcessCount());
    }

    @Override
    public java.util.List<SequenceChecker> getSequenceChecker() {
        return null;
    }

    protected static Map<String, Boolean> hookOnlyMap(int processCount) {
        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
        for (int i = 1; i <= processCount; i++) {
            map.put(FischerModelUtil.enterHookChannel(i), true);
            map.put(FischerModelUtil.exitHookChannel(i), true);
        }
        return map;
    }

    protected static void addProcessInterface(Map<String, Boolean> map, int processIndex) {
        map.put(FischerModelUtil.tryChannel(processIndex), false);
        map.put(FischerModelUtil.setChannel(processIndex), false);
        map.put(FischerModelUtil.enterChannel(processIndex), false);
        map.put(FischerModelUtil.exitChannel(processIndex), false);
        map.put(FischerModelUtil.retryChannel(processIndex), false);
    }
}
