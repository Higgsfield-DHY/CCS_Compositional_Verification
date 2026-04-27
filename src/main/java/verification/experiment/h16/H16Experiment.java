package verification.experiment.h16;

import verification.experiment.Experiment;
import verification.frame.Cq2Mode;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.util.PortPreprocessConfig;
import verification.util.PortSplitMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class H16Experiment extends Experiment {
    @Override
    public String getStatement() {
        return "A[] obs != ERROR";
    }

    @Override
    public Map<String, Boolean> getSyncSendMap() {
        // Port-mode H1-6 does not use legacy direction mapping.
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getResetSigma() {
        return Collections.singleton("a_m1_to_m2?");
    }

    @Override
    public List<Template> getM1() {
        return Arrays.asList(
                H16ModelUtil.buildH1(),
                H16ModelUtil.buildH2()
        );
    }

    @Override
    public List<Template> getM2() {
        return Arrays.asList(
                H16ModelUtil.buildH3(),
                H16ModelUtil.buildH4(),
                H16ModelUtil.buildH5(),
                H16ModelUtil.buildH6()
        );
    }

    @Override
    public String getNtaPath() {
        return ".\\src\\main\\resources\\verification\\h1_6-source.xml";
    }

    @Override
    public Declaration getGlobalDeclaration() {
        return H16ModelUtil.buildGlobalDeclaration();
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
        Set<String> sigma = new HashSet<>();
        sigma.add("a_m1_to_m2?");
        sigma.add("b!");
        return sigma;
    }

    @Override
    public Map<String, String> getM2RenameMap() {
        return Collections.emptyMap();
    }

    @Override
    public PortPreprocessConfig getPortPreprocessConfig() {
        return PortPreprocessConfig.of(
                PortSplitMode.BIDIRECTIONAL_DOMAIN_SPLIT,
                Collections.singleton("a"),
                true);
    }

    @Override
    public Cq2Mode getCq2Mode() {
        return Cq2Mode.LEGACY_SINK;
    }

    @Override
    public void execute(boolean tableLearner, boolean guessSigma, boolean sequenceCheck, int repeatCount) throws IOException {
        super.execute(tableLearner, false, false, repeatCount);
    }
}
