package verification.experiment.train_gate_channel;

import verification.experiment.Experiment;
import verification.experiment.channel.ChannelPortMapper;
import verification.experiment.train_gate.TrainGateModelUtil;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class TrainGateChannelExperiment extends Experiment {
    protected abstract int getTrainCount();

    @Override
    public String getStatement() {
        return "A[] Queue.list[N-1] == 0";
    }

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
    public Map<String, Boolean> getSyncSendMap() {
        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
        map.put("appr", true);
        map.put("stop", false);
        map.put("go", false);
        map.put("leave", true);
        return map;
    }

    @Override
    public Declaration getGlobalDeclaration() {
        return TrainGateModelUtil.buildGlobalDeclaration(getTrainCount() + 1);
    }

    @Override
    public List<Template> getM1() {
        List<Template> list = new ArrayList<Template>();
        list.add(TrainGateModelUtil.buildGate());
        list.add(TrainGateModelUtil.buildIntQueue());
        return list;
    }

    @Override
    public List<Template> getM2() {
        List<Template> list = new ArrayList<Template>();
        for (int i = 1; i <= getTrainCount(); i++) {
            list.add(TrainGateModelUtil.buildTrain(i));
        }
        return list;
    }

    @Override
    public List<SequenceChecker> getSequenceChecker() {
        return null;
    }
}
