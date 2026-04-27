package verification.experiment.threea;

import verification.experiment.Experiment;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.util.PortPreprocessConfig;
import verification.util.PortSplitMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThreeAExperiment extends Experiment {
    @Override
    public String getStatement() {
        return "A[] obs != ERROR";
    }

    @Override
    public Map<String, Boolean> getSyncSendMap() {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getResetSigma() {
        Set<String> resetSigma = new LinkedHashSet<String>();
        resetSigma.add("a_m1_to_m2?");
        resetSigma.add("a_m2_to_m1!");
        return resetSigma;
    }

    @Override
    public List<Template> getM1() {
        return Arrays.asList(
                ThreeAExampleModelUtil.buildM1Send("M1_S1"),
                ThreeAExampleModelUtil.buildM1Receive(),
                ThreeAExampleModelUtil.buildM1Send("M1_S2")
        );
    }

    @Override
    public List<Template> getM2() {
        return Arrays.asList(
                ThreeAExampleModelUtil.buildM2Receive("M2_R1"),
                ThreeAExampleModelUtil.buildM2Send(),
                ThreeAExampleModelUtil.buildM2Receive("M2_R2")
        );
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\threea-source.xml";
    }

    @Override
    public Declaration getGlobalDeclaration() {
        return ThreeAExampleModelUtil.buildGlobalDeclaration();
    }

    @Override
    public List<SequenceChecker> getSequenceChecker() {
        return null;
    }

    @Override
    public boolean isPortActionMode() {
        return true;
    }

    @Override
    public Set<String> getTargetSigma() {
        Set<String> sigma = new LinkedHashSet<String>();
        sigma.add("a_m1_to_m2?");
        sigma.add("a_m2_to_m1!");
        return sigma;
    }

    @Override
    public PortPreprocessConfig getPortPreprocessConfig() {
        return PortPreprocessConfig.of(
                PortSplitMode.BIDIRECTIONAL_DOMAIN_SPLIT,
                Collections.singleton("a"),
                true);
    }
}

