package verification.experiment.random_channel;

import verification.experiment.Experiment;

import java.io.File;
import java.util.Locale;

public class RandomChannelCase {
    private final File caseDir;
    private final RandomChannelSpec spec;
    private final RandomChannelManifest manifest;

    public RandomChannelCase(File caseDir, RandomChannelSpec spec, RandomChannelManifest manifest) {
        this.caseDir = caseDir;
        this.spec = spec;
        this.manifest = manifest;
    }

    public File getCaseDir() {
        return caseDir;
    }

    public RandomChannelSpec getSpec() {
        return spec;
    }

    public RandomChannelManifest getManifest() {
        return manifest;
    }

    public String getSuiteId() {
        return spec.getSuiteId();
    }

    public String getCaseId() {
        return spec.getCaseId();
    }

    public String getFamily() {
        return spec.getFamily();
    }

    public String getSuitePurpose() {
        return manifest.getSuitePurpose();
    }

    public String getTopologyKind() {
        return manifest.getTopologyKind();
    }

    public String getStructureGroup() {
        return manifest.getStructureGroup();
    }

    public int getAlphabetSize() {
        return manifest.getAlphabetSize();
    }

    public String getVariantId() {
        return manifest.getVariantId();
    }

    public String getCaseProfile() {
        return manifest.getCaseProfile();
    }

    public String getShowcaseTarget() {
        return manifest.getShowcaseTarget();
    }

    public String getModePattern() {
        return manifest.getModePattern();
    }

    public int getBurstLength() {
        return manifest.getBurstLength();
    }

    public int getModeCount() {
        return manifest.getModeCount();
    }

    public String getPropertyText() {
        return manifest.getPropertyText();
    }

    public String getDescription() {
        return manifest.getDescription();
    }

    public String getM1Desc() {
        return manifest.getM1Desc();
    }

    public String getM2Desc() {
        return manifest.getM2Desc();
    }

    public String getExpectedPreprocess() {
        return manifest.getExpectedPreprocess();
    }

    public Experiment newExperiment() {
        return new RandomChannelExperiment(caseDir, spec);
    }

    public String normalizedKey() {
        return getCaseId().toLowerCase(Locale.ROOT).trim();
    }
}
