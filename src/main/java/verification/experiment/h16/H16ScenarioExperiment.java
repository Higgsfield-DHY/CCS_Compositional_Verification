package verification.experiment.h16;

import verification.experiment.Experiment;
import verification.frame.Cq2Mode;
import verification.plugins.SequenceChecker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.util.PrimeSplitConfig;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class H16ScenarioExperiment extends Experiment {
    private final H16ScenarioConfig config;

    public H16ScenarioExperiment(H16ScenarioConfig config) {
        this.config = config;
    }

    public H16ScenarioConfig getConfig() {
        return config;
    }

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
        return Collections.singleton("a?");
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
                H16ModelUtil.buildH3(config.getH3Guard()),
                H16ModelUtil.buildH4(config.getH4Guard()),
                H16ModelUtil.buildH5(config.getH5Channel(), config.getH5Guard()),
                H16ModelUtil.buildH6(config.getH6Channel(), config.getH6Guard())
        );
    }

    @Override
    public String getNtaPath() {
        String suffix = config.getCaseName().toLowerCase().replaceAll("[^a-z0-9_\\-]", "_");
        return ".\\src\\main\\resources\\verification\\h1_6-" + suffix + ".xml";
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
        sigma.add("a?");
        sigma.add("b!");
        return sigma;
    }

    @Override
    public Map<String, String> getM2RenameMap() {
        return config.getM2RenameMap();
    }

    @Override
    public PrimeSplitConfig getPrimeSplitConfig() {
        return PrimeSplitConfig.of("a");
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
