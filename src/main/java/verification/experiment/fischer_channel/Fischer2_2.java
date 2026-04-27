package verification.experiment.fischer_channel;

import verification.experiment.fischer.FischerModelUtil;
import verification.uppaal.model.Template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Fischer2_2 extends FischerChannelExperiment {
    @Override
    protected int getProcessCount() {
        return 2;
    }

    @Override
    public String getStatement() {
        return "A[] Mutex.hold <= 1";
    }

    @Override
    public Map<String, Boolean> getSyncSendMap() {
        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
        addProcessInterface(map, 1);
        return map;
    }

    @Override
    public List<Template> getM1() {
        List<Template> list = new ArrayList<Template>();
        list.add(FischerModelUtil.buildMutexMonitor(2));
        list.add(FischerModelUtil.buildTa3Process(1));
        return list;
    }

    @Override
    public List<Template> getM2() {
        List<Template> list = new ArrayList<Template>();
        list.add(FischerModelUtil.buildTa3Process(2));
        list.add(FischerModelUtil.buildGlobalVar(2));
        return list;
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\fischer2_channel_case2.xml";
    }
}
