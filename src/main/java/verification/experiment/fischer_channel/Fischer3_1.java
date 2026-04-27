package verification.experiment.fischer_channel;

import verification.experiment.fischer.FischerModelUtil;
import verification.uppaal.model.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Fischer3_1 extends FischerChannelExperiment {
    @Override
    protected int getProcessCount() {
        return 3;
    }

    @Override
    public String getStatement() {
        return "A[] Mutex.hold <= 1";
    }

    @Override
    public Map<String, Boolean> getSyncSendMap() {
        return hookOnlyMap(3);
    }

    @Override
    public List<Template> getM1() {
        List<Template> list = new ArrayList<Template>();
        list.add(FischerModelUtil.buildMutexMonitor(3));
        return list;
    }

    @Override
    public List<Template> getM2() {
        List<Template> list = new ArrayList<Template>();
        list.add(FischerModelUtil.buildTa4Process(1));
        list.add(FischerModelUtil.buildTa4Process(2));
        list.add(FischerModelUtil.buildTa4Process(3));
        list.add(FischerModelUtil.buildGlobalVar(3));
        return list;
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\fischer3_channel_case1.xml";
    }
}
