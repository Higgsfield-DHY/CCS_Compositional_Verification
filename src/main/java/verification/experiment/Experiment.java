package verification.experiment;

import learn.classificationtree.ClassificationTree;
import learn.frame.Learner;
import learn.observationTable.NewObserbationTable;
import ta.ota.ResetLogicTimeWord;
import verification.frame.CheckFrame;
import verification.frame.Cq2Mode;
import verification.plugins.SequenceChecker;
import verification.report.AgRunReport;
import verification.reset.ResetHeuristicConfig;
import verification.reset.ResetPolicyType;
import verification.teacher.UppaalTeacher;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.NTA;
import verification.uppaal.model.Template;
import verification.util.ChannelPreprocessor;
import verification.util.ChannelAliasRegistry;
import verification.util.DisplayAliasContext;
import verification.util.PortPreprocessConfig;
import verification.util.PrimeSplitConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Experiment {
    public abstract String getStatement();

    public abstract Map<String, Boolean> getSyncSendMap();

    public abstract Set<String> getResetSigma();

    public abstract List<Template> getM1();

    public abstract List<Template> getM2() throws IOException;

    public abstract String getNtaPath();

    public abstract Declaration getGlobalDeclaration();

    public abstract List<SequenceChecker> getSequenceChecker();

    public boolean isPortActionMode() {
        return false;
    }

    public Set<String> getTargetSigma() {
        return new HashSet<>(getSyncSendMap().keySet());
    }

    public Map<String, String> getM1RenameMap() {
        return Collections.emptyMap();
    }

    public Map<String, String> getM2RenameMap() {
        return Collections.emptyMap();
    }

    public PrimeSplitConfig getPrimeSplitConfig() {
        return PrimeSplitConfig.empty();
    }

    public PortPreprocessConfig getPortPreprocessConfig() {
        PrimeSplitConfig primeSplitConfig = getPrimeSplitConfig();
        if (primeSplitConfig == null || primeSplitConfig.isEmpty()) {
            return PortPreprocessConfig.empty();
        }
        return PortPreprocessConfig.primeSplit(primeSplitConfig.getChannels());
    }

    public Cq2Mode getCq2Mode() {
        return Cq2Mode.LEGACY_SINK;
    }

    public void execute(boolean tableLearner, boolean guessSigma, boolean sequenceCheck, int repeatCount) throws IOException {
        CheckFrame check = buildCheckFrame(tableLearner, guessSigma, sequenceCheck);
        try {
            check.start(repeatCount);
        } finally {
            DisplayAliasContext.clear();
        }
    }

    public AgRunReport executeWithReport(boolean tableLearner, boolean guessSigma,
                                         boolean sequenceCheck, int repeatCount) throws IOException {
        CheckFrame check = buildCheckFrame(tableLearner, guessSigma, sequenceCheck);
        AgRunReport last = null;
        int rounds = Math.max(1, repeatCount);
        try {
            for (int i = 0; i < rounds; i++) {
                String runName = getClass().getSimpleName();
                if (rounds > 1) {
                    runName = runName + "#" + (i + 1);
                }
                check.getTeacher().beginRunReport(runName);
                last = check.startWithReport();
                if (last != null) {
                    last.setResetPolicyType(getResetPolicyType().name());
                }
            }
        } finally {
            DisplayAliasContext.clear();
        }
        return last;
    }

    private CheckFrame buildCheckFrame(boolean tableLearner, boolean guessSigma, boolean sequenceCheck) throws IOException {
        Map<String, Boolean> syncSendMap = getSyncSendMap();
        Set<String> targetSigma = new HashSet<String>(getTargetSigma());
        if (targetSigma.isEmpty() && !isPortActionMode()) {
            targetSigma = new HashSet<String>(syncSendMap.keySet());
        }
        Set<String> resetSigma = getResetSigma();

        Declaration globalDeclaration = getGlobalDeclaration();
        List<Template> m1 = getM1();
        List<Template> m2 = getM2();
        ChannelAliasRegistry aliasRegistry = new ChannelAliasRegistry();

        if (isPortActionMode()) {
            aliasRegistry = ChannelPreprocessor.preprocessPortMode(
                    globalDeclaration, m1, m2, getM1RenameMap(), getM2RenameMap(), targetSigma, getPortPreprocessConfig());
        }
        DisplayAliasContext.set(aliasRegistry);

        List<Template> system = new ArrayList<>(m1);
        system.addAll(m2);
        NTA nta = new NTA(globalDeclaration, system);
        nta.writeToUppaalXml(getNtaPath());

        UppaalTeacher teacher = new UppaalTeacher(m1, m2, getStatement(), globalDeclaration,
                syncSendMap, resetSigma, targetSigma, isPortActionMode(),
                getResetPolicyType(), getResetHeuristicConfig(), getCq2Mode());
        if (sequenceCheck) {
            teacher.setSequencePlugin(getSequenceChecker());
        }

        Learner<ResetLogicTimeWord> learner;
        if (tableLearner) {
            learner = new NewObserbationTable("assume", targetSigma, teacher);
        } else {
            learner = new ClassificationTree("assume", targetSigma, teacher);
        }

        CheckFrame check = new CheckFrame(teacher, learner);
        if (guessSigma) {
            check.guessSigmas(targetSigma);
        }
        return check;
    }

    public ResetPolicyType getResetPolicyType() {
        return ResetPolicyType.STATIC_SIGMA;
    }

    public ResetHeuristicConfig getResetHeuristicConfig() {
        return ResetHeuristicConfig.fromSeedActions(getResetSigma());
    }
}
