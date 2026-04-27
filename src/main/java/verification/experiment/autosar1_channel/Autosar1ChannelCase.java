package verification.experiment.autosar1_channel;

import verification.experiment.Experiment;

import java.util.Locale;

public class Autosar1ChannelCase {
    private final String caseId;
    private final String sourceCase;
    private final String structureGroup;
    private final String partitionNote;
    private final String m1Desc;
    private final String m2Desc;
    private final String verifyGoal;
    private final CaseFactory factory;

    public Autosar1ChannelCase(String caseId,
                               String sourceCase,
                               String structureGroup,
                               String partitionNote,
                               String m1Desc,
                               String m2Desc,
                               String verifyGoal,
                               CaseFactory factory) {
        this.caseId = caseId;
        this.sourceCase = sourceCase;
        this.structureGroup = structureGroup;
        this.partitionNote = partitionNote;
        this.m1Desc = m1Desc;
        this.m2Desc = m2Desc;
        this.verifyGoal = verifyGoal;
        this.factory = factory;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getSourceCase() {
        return sourceCase;
    }

    public String getStructureGroup() {
        return structureGroup;
    }

    public String getPartitionNote() {
        return partitionNote;
    }

    public String getM1Desc() {
        return m1Desc;
    }

    public String getM2Desc() {
        return m2Desc;
    }

    public String getVerifyGoal() {
        return verifyGoal;
    }

    public Experiment newExperiment() {
        return factory.create();
    }

    public String normalizedKey() {
        return caseId.toLowerCase(Locale.ROOT).trim();
    }

    public interface CaseFactory {
        Experiment create();
    }
}
