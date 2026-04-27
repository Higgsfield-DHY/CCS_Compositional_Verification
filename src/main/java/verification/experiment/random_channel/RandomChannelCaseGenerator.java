package verification.experiment.random_channel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import verification.experiment.channel.ChannelCaseMetricsUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class RandomChannelCaseGenerator {
    private static final int SANITY_CASES_PER_SUITE = 12;
    private static final int PERFORMANCE_CASES_PER_SUITE = 8;
    private static final int MID_CASES_PER_SUITE = 10;
    private static final int LEARNING_CASES_PER_SUITE = 9;
    private static final int DEFAULT_TIMEOUT_MS = 120000;

    private static final String PURPOSE_SANITY = "SANITY";
    private static final String PURPOSE_PERFORMANCE = "PERFORMANCE";

    private static final String TOPOLOGY_SIMPLE_BUFFER = "SIMPLE_BUFFER";
    private static final String TOPOLOGY_SIMPLE_MUTEX = "SIMPLE_MUTEX";
    private static final String TOPOLOGY_SPLIT_BUFFER = "SPLIT_BUFFER";
    private static final String TOPOLOGY_BOUNDARY_SCHED_BUFFER = "BOUNDARY_SCHED_BUFFER";
    private static final String TOPOLOGY_BOUNDARY_PIPELINE_BUFFER = "BOUNDARY_PIPELINE_BUFFER";
    private static final String TOPOLOGY_BOUNDARY_SPLIT_PIPELINE_BUFFER = "BOUNDARY_SPLIT_PIPELINE_BUFFER";
    private static final String TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER = "MID_BOUNDARY_SCHED_BUFFER";
    private static final String TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER = "MID_BOUNDARY_PIPELINE_BUFFER";
    private static final String TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER = "MID_BOUNDARY_SPLIT_PIPELINE_BUFFER";
    private static final String MODE_ONE = "ONE_MODE";
    private static final String MODE_TWO = "TWO_MODE";
    private static final String MODE_THREE = "THREE_MODE";
    private static final String PROFILE_BASE_A = "BASE_A";
    private static final String PROFILE_BASE_B = "BASE_B";
    private static final String PROFILE_TIGHT_A = "TIGHT_A";
    private static final String PROFILE_TIGHT_B = "TIGHT_B";
    private static final String PROFILE_TWO_A = "TWO_A";
    private static final String PROFILE_TWO_B = "TWO_B";
    private static final String PROFILE_S2_THREE = "S2_THREE";
    private static final String PROFILE_S4_TWO = "S4_TWO";
    private static final String PROFILE_S4_THREE = "S4_THREE";
    private static final String PROFILE_S6_STRESS = "S6_STRESS";
    private static final String PROFILE_Q2_R1 = "Q2_R1";
    private static final String PROFILE_Q3_R1 = "Q3_R1";
    private static final String PROFILE_Q3_R2 = "Q3_R2";
    private static final String SHOWCASE_Q2_R1 = "Q2_R1";
    private static final String SHOWCASE_Q3_R1 = "Q3_R1";
    private static final String SHOWCASE_Q3_R2 = "Q3_R2";

    private RandomChannelCaseGenerator() {
    }

    public static void main(String[] args) throws Exception {
        generateAll();
    }

    public static void generateAll() throws Exception {
        File root = new File(RandomChannelCases.ROOT_DIR);
        ensureDir(root);
        ensureResultDirs(new File(RandomChannelCases.G_RESULT_DIR));
        ensureResultDirs(new File(RandomChannelCases.P_RESULT_DIR));
        ensureResultDirs(new File(RandomChannelCases.M_RESULT_DIR));

        generateSanitySuites(root);
        generatePerformanceSuites(root);
        generateMainPerformanceSuites(root);
        generateLearningPerformanceSuites(root);
    }

    public static void generateSanitySuites(File root) throws Exception {
        generateSuite("G1_clean_small", buildG1Cases(), root);
        generateSuite("G2_clean_multi", buildG2Cases(), root);
        generateSuite("G3_split_required", buildG3Cases(), root);
    }

    public static void generatePerformanceSuites(File root) throws Exception {
        generateSuite("P1_boundary_sched_buffer", buildP1Cases(), root);
        generateSuite("P2_boundary_pipeline_buffer", buildP2Cases(), root);
        generateSuite("P3_boundary_split_pipeline_buffer", buildP3Cases(), root);
    }

    public static void generateMainPerformanceSuites(File root) throws Exception {
        generateSuite("M1_boundary_sched_buffer", buildM1Cases(), root);
        generateSuite("M2_boundary_pipeline_buffer", buildM2Cases(), root);
        generateSuite("M3_boundary_split_pipeline_buffer", buildM3Cases(), root);
    }

    public static void generateLearningPerformanceSuites(File root) throws Exception {
        generateSuite("M4_nontrivial_learning", buildM4Cases(), root);
    }

    private static void generateSuite(String suiteDir, List<RandomChannelSpec> specs, File root) throws Exception {
        File suiteRoot = new File(root, suiteDir);
        resetDir(suiteRoot);
        for (int i = 0; i < specs.size(); i++) {
            File caseDir = new File(suiteRoot, "case" + i);
            ensureDir(caseDir);
            RandomChannelSpec spec = specs.get(i);
            RandomChannelManifest manifest = buildManifest(caseDir, spec);
            writeJson(new File(caseDir, "spec.json"), spec);
            writeJson(new File(caseDir, "manifest.json"), manifest);
        }
    }

    private static RandomChannelManifest buildManifest(File caseDir, RandomChannelSpec spec) throws IOException {
        RandomChannelExperiment experiment = new RandomChannelExperiment(caseDir, spec);
        ChannelCaseMetricsUtil.CaseMetrics metrics = ChannelCaseMetricsUtil.analyze(experiment);

        RandomChannelManifest manifest = new RandomChannelManifest();
        manifest.setSuiteId(spec.getSuiteId());
        manifest.setCaseId(spec.getCaseId());
        manifest.setSeed(spec.getSeed());
        manifest.setFamily(spec.getFamily());
        manifest.setSuitePurpose(spec.getSuitePurpose());
        manifest.setTopologyKind(spec.getTopologyKind());
        manifest.setModelKind(spec.getModelKind());
        manifest.setPropertyId(spec.getPropertyId());
        manifest.setDescription(spec.getDescription());
        manifest.setStructureGroup(spec.getStructureGroup());
        manifest.setAlphabetSize(spec.getAlphabetSize());
        manifest.setVariantId(spec.getVariantId());
        manifest.setCaseProfile(spec.getCaseProfile());
        manifest.setShowcaseTarget(spec.getShowcaseTarget());
        manifest.setModePattern(spec.getModePattern());
        manifest.setBurstLength(spec.getBurstLength());
        manifest.setModeCount(spec.getModeCount());
        manifest.setPropertyText(experiment.getStatement());
        manifest.setM1Desc(describeM1(spec));
        manifest.setM2Desc(describeM2(spec));
        manifest.setExpectedPreprocess(spec.getExpectedPreprocess());
        manifest.setM1ComponentCount(1);
        manifest.setM2ComponentCount(resolveM2ComponentCount(spec));
        manifest.setM2ClockCount(metrics.m2ClockCount);
        manifest.setM2AlphabetSize(metrics.m2AlphabetSize);
        manifest.setInterfaceSigmaSize(experiment.getTargetSigma().size());
        manifest.setExpectedAgFriendly(PURPOSE_PERFORMANCE.equals(spec.getSuitePurpose()));
        manifest.setM2LocalStateSpace(toStateSpace(metrics.m2LocalStateSpace));
        return manifest;
    }

    private static int resolveM2ComponentCount(RandomChannelSpec spec) {
        String topology = spec.getTopologyKind();
        if (TOPOLOGY_BOUNDARY_SCHED_BUFFER.equals(topology)) {
            return spec.getWriterCount() + spec.getReaderCount() + 1;
        }
        if (TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER.equals(topology)) {
            return spec.getWriterCount() + spec.getReaderCount() + 1;
        }
        if (TOPOLOGY_BOUNDARY_PIPELINE_BUFFER.equals(topology)
                || TOPOLOGY_BOUNDARY_SPLIT_PIPELINE_BUFFER.equals(topology)
                || TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER.equals(topology)
                || TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER.equals(topology)) {
            return 2 * spec.getWriterCount() + 2 * spec.getReaderCount() + 1;
        }
        return spec.getM2Spec() == null ? 0 : spec.getM2Spec().size();
    }

    private static String toStateSpace(BigInteger value) {
        return value == null ? "0" : value.toString();
    }

    private static String describeM1(RandomChannelSpec spec) {
        if ("BUFFER".equals(spec.getModelKind())) {
            return spec.getM1Spec().getName() + "(buffer,len=" + spec.getM1Spec().getLen() + ")";
        }
        return spec.getM1Spec().getName() + "(mutex,clients=" + spec.getM1Spec().getClientCount() + ")";
    }

    private static String describeM2(RandomChannelSpec spec) {
        String topology = spec.getTopologyKind();
        if (TOPOLOGY_BOUNDARY_SCHED_BUFFER.equals(topology)) {
            return spec.getWriterCount() + "xWriterTask + "
                    + spec.getReaderCount() + "xReaderTask + Dispatcher"
                    + " (phaseDepth=" + spec.getPhaseDepth()
                    + ", dispatcherDepth=" + spec.getDispatcherDepth() + ")";
        }
        if (TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER.equals(topology)) {
            return spec.getWriterCount() + "xWriterTask + "
                    + spec.getReaderCount() + "xReaderTask + Dispatcher"
                    + " (phaseDepth=" + spec.getPhaseDepth()
                    + ", dispatcherDepth=" + spec.getDispatcherDepth()
                    + ", sigma=" + spec.getAlphabetSize()
                    + ", profile=" + spec.getCaseProfile()
                    + ", mode=" + spec.getModePattern()
                    + ", burst=" + spec.getBurstLength() + ")";
        }
        if (TOPOLOGY_BOUNDARY_PIPELINE_BUFFER.equals(topology)) {
            return spec.getWriterCount() + "xWriterController + "
                    + spec.getWriterCount() + "xWriterCommitter + "
                    + spec.getReaderCount() + "xReaderController + "
                    + spec.getReaderCount() + "xReaderCommitter + Dispatcher"
                    + " (phaseDepth=" + spec.getPhaseDepth()
                    + ", dispatcherDepth=" + spec.getDispatcherDepth() + ")";
        }
        if (TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER.equals(topology)) {
            return spec.getWriterCount() + "xWriterController + "
                    + spec.getWriterCount() + "xWriterCommitter + "
                    + spec.getReaderCount() + "xReaderController + "
                    + spec.getReaderCount() + "xReaderCommitter + Dispatcher"
                    + " (phaseDepth=" + spec.getPhaseDepth()
                    + ", dispatcherDepth=" + spec.getDispatcherDepth()
                    + ", sigma=" + spec.getAlphabetSize()
                    + ", profile=" + spec.getCaseProfile()
                    + ", mode=" + spec.getModePattern()
                    + ", burst=" + spec.getBurstLength() + ")";
        }
        if (TOPOLOGY_BOUNDARY_SPLIT_PIPELINE_BUFFER.equals(topology)) {
            return spec.getWriterCount() + "xWriterController + "
                    + spec.getWriterCount() + "xWriterCommitter + "
                    + spec.getReaderCount() + "xReaderController + "
                    + spec.getReaderCount() + "xReaderCommitter + Dispatcher"
                    + " (phaseDepth=" + spec.getPhaseDepth()
                    + ", dispatcherDepth=" + spec.getDispatcherDepth() + ")"
                    + " (split=" + spec.getSplitChannel() + ")";
        }
        if (TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER.equals(topology)) {
            return spec.getWriterCount() + "xWriterController + "
                    + spec.getWriterCount() + "xWriterCommitter + "
                    + spec.getReaderCount() + "xReaderController + "
                    + spec.getReaderCount() + "xReaderCommitter + Dispatcher"
                    + " (phaseDepth=" + spec.getPhaseDepth()
                    + ", dispatcherDepth=" + spec.getDispatcherDepth()
                    + ", sigma=" + spec.getAlphabetSize()
                    + ", profile=" + spec.getCaseProfile()
                    + ", mode=" + spec.getModePattern()
                    + ", burst=" + spec.getBurstLength() + ")"
                    + " (split=" + spec.getSplitChannel() + ")";
        }
        List<String> names = new ArrayList<String>();
        for (RandomChannelSpec.ActorSpec actor : spec.getM2Spec()) {
            names.add(actor.getName());
        }
        return join(names);
    }

    private static List<RandomChannelSpec> buildG1Cases() {
        List<RandomChannelSpec> specs = new ArrayList<RandomChannelSpec>();
        for (int i = 0; i < SANITY_CASES_PER_SUITE; i++) {
            long seed = 1100L + i;
            Random random = new Random(seed);
            if (i < 4) {
                specs.add(buildSmallNonNegativeCase("G1_clean_small", "G1", i, seed, random, i % 2 == 0));
            } else if (i < 8) {
                specs.add(buildSmallBoundedCase("G1_clean_small", "G1", i, seed, random, i % 2 == 0));
            } else {
                specs.add(buildSmallMutexCase("G1_clean_small", "G1", i, seed, random, i % 2 == 0));
            }
        }
        return specs;
    }

    private static List<RandomChannelSpec> buildG2Cases() {
        List<RandomChannelSpec> specs = new ArrayList<RandomChannelSpec>();
        for (int i = 0; i < SANITY_CASES_PER_SUITE; i++) {
            long seed = 2100L + i;
            Random random = new Random(seed);
            if (i < 4) {
                specs.add(buildLargeNonNegativeCase("G2_clean_multi", "G2", i, seed, random, i % 2 == 0));
            } else if (i < 8) {
                specs.add(buildLargeBoundedCase("G2_clean_multi", "G2", i, seed, random, i % 2 == 0));
            } else {
                specs.add(buildLargeMutexCase("G2_clean_multi", "G2", i, seed, random, i % 2 == 0));
            }
        }
        return specs;
    }

    private static List<RandomChannelSpec> buildG3Cases() {
        List<RandomChannelSpec> specs = new ArrayList<RandomChannelSpec>();
        for (int i = 0; i < SANITY_CASES_PER_SUITE; i++) {
            long seed = 3100L + i;
            Random random = new Random(seed);
            if (i < 6) {
                specs.add(buildSplitNonNegativeCase("G3_split_required", "G3", i, seed, random, i % 2 == 0));
            } else {
                specs.add(buildSplitBoundedCase("G3_split_required", "G3", i, seed, random, i % 2 == 0));
            }
        }
        return specs;
    }

    private static List<RandomChannelSpec> buildP1Cases() {
        List<RandomChannelSpec> specs = new ArrayList<RandomChannelSpec>();
        int[][] grid = {
                {6, 6, 2, 3, 4},
                {6, 7, 2, 3, 5},
                {7, 6, 3, 4, 5},
                {7, 7, 3, 4, 6}
        };
        for (int i = 0; i < PERFORMANCE_CASES_PER_SUITE; i++) {
            int[] tuple = grid[i % grid.length];
            specs.add(buildPerformanceBufferCase(
                    "P1_boundary_sched_buffer", "P1", i, 4100L + i,
                    TOPOLOGY_BOUNDARY_SCHED_BUFFER,
                    "boundary scheduler buffer safe",
                    "M1-single/M2-boundary-scheduler-buffer",
                    tuple[0], tuple[1], tuple[2], tuple[3], tuple[4], null));
        }
        return specs;
    }

    private static List<RandomChannelSpec> buildP2Cases() {
        List<RandomChannelSpec> specs = new ArrayList<RandomChannelSpec>();
        int[][] grid = {
                {5, 5, 2, 3, 4},
                {5, 6, 2, 3, 5},
                {6, 5, 3, 4, 5},
                {6, 6, 3, 4, 6}
        };
        for (int i = 0; i < PERFORMANCE_CASES_PER_SUITE; i++) {
            int[] tuple = grid[i % grid.length];
            specs.add(buildPerformanceBufferCase(
                    "P2_boundary_pipeline_buffer", "P2", i, 5100L + i,
                    TOPOLOGY_BOUNDARY_PIPELINE_BUFFER,
                    "boundary pipeline buffer safe",
                    "M1-single/M2-boundary-pipeline-buffer",
                    tuple[0], tuple[1], tuple[2], tuple[3], tuple[4], null));
        }
        return specs;
    }

    private static List<RandomChannelSpec> buildP3Cases() {
        List<RandomChannelSpec> specs = new ArrayList<RandomChannelSpec>();
        int[][] grid = {
                {5, 5, 2, 3, 4},
                {5, 6, 2, 3, 5},
                {6, 5, 3, 4, 5},
                {6, 6, 3, 4, 6}
        };
        for (int i = 0; i < PERFORMANCE_CASES_PER_SUITE; i++) {
            int[] tuple = grid[i % grid.length];
            String splitChannel = i < 4 ? "read" : "write";
            specs.add(buildPerformanceBufferCase(
                    "P3_boundary_split_pipeline_buffer", "P3", i, 6100L + i,
                    TOPOLOGY_BOUNDARY_SPLIT_PIPELINE_BUFFER,
                    "boundary split pipeline buffer safe",
                    "M1-single/M2-boundary-split-pipeline-buffer",
                    tuple[0], tuple[1], tuple[2], tuple[3], tuple[4], splitChannel));
        }
        return specs;
    }

    private static List<RandomChannelSpec> buildM1Cases() {
        List<RandomChannelSpec> specs = new ArrayList<RandomChannelSpec>();
        specs.add(buildMProfileCase("M1_boundary_sched_buffer", "M1", "M1_s2_base_a", 7120L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "mid boundary scheduler buffer",
                "M1-single/M2-main-scheduler-buffer", 2, 2, 2, 2,
                PROFILE_BASE_A, 3, 1, null, "MODERATE", MODE_ONE, 1, 1));
        specs.add(buildMProfileCase("M1_boundary_sched_buffer", "M1", "M1_s2_base_b", 7121L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "mid boundary scheduler buffer",
                "M1-single/M2-main-scheduler-buffer", 2, 2, 2, 2,
                PROFILE_BASE_B, 4, 1, null, "MODERATE", MODE_ONE, 1, 1));
        specs.add(buildMProfileCase("M1_boundary_sched_buffer", "M1", "M1_s2_tight_a", 7122L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "mid boundary scheduler buffer",
                "M1-single/M2-main-scheduler-buffer", 2, 2, 2, 2,
                PROFILE_TIGHT_A, 4, 1, null, "TIGHT", MODE_ONE, 1, 1));
        specs.add(buildMProfileCase("M1_boundary_sched_buffer", "M1", "M1_s2_tight_b", 7123L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "mid boundary scheduler buffer",
                "M1-single/M2-main-scheduler-buffer", 2, 2, 2, 2,
                PROFILE_TIGHT_B, 4, 1, null, "TIGHT", MODE_ONE, 1, 1));
        specs.add(buildMProfileCase("M1_boundary_sched_buffer", "M1", "M1_s2_two_a", 7124L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "mid boundary scheduler buffer",
                "M1-single/M2-main-scheduler-buffer", 2, 2, 2, 2,
                PROFILE_TWO_A, 3, 2, null, "MODERATE", MODE_TWO, 1, 2));
        specs.add(buildMProfileCase("M1_boundary_sched_buffer", "M1", "M1_s2_two_b", 7125L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "mid boundary scheduler buffer",
                "M1-single/M2-main-scheduler-buffer", 2, 2, 2, 2,
                PROFILE_TWO_B, 4, 2, null, "MODERATE", MODE_TWO, 1, 2));
        specs.add(buildMProfileCase("M1_boundary_sched_buffer", "M1", "M1_s2_three", 7126L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "mid boundary scheduler buffer",
                "M1-single/M2-main-scheduler-buffer", 2, 2, 2, 2,
                PROFILE_S2_THREE, 3, 2, null, "MODERATE", MODE_THREE, 1, 3));
        specs.add(buildMProfileCase("M1_boundary_sched_buffer", "M1", "M1_s4_two", 7127L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "mid boundary scheduler buffer",
                "M1-single/M2-main-scheduler-buffer", 2, 2, 2, 4,
                PROFILE_S4_TWO, 3, 2, null, "MODERATE", MODE_TWO, 1, 2));
        specs.add(buildMProfileCase("M1_boundary_sched_buffer", "M1", "M1_s4_three", 7128L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "mid boundary scheduler buffer",
                "M1-single/M2-main-scheduler-buffer", 2, 2, 2, 4,
                PROFILE_S4_THREE, 4, 2, null, "MODERATE", MODE_THREE, 1, 3));
        specs.add(buildMProfileCase("M1_boundary_sched_buffer", "M1", "M1_s6_stress", 7129L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "mid boundary scheduler buffer",
                "M1-single/M2-main-scheduler-buffer", 3, 3, 2, 6,
                PROFILE_S6_STRESS, 2, 1, null, "MODERATE", MODE_THREE, 1, 3));
        return specs;
    }

    private static List<RandomChannelSpec> buildM2Cases() {
        List<RandomChannelSpec> specs = new ArrayList<RandomChannelSpec>();
        specs.add(buildMProfileCase("M2_boundary_pipeline_buffer", "M2", "M2_s2_base_a", 8120L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "mid boundary pipeline buffer",
                "M1-single/M2-main-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_BASE_A, 1, 1, null, "MODERATE", MODE_ONE, 1, 1));
        specs.add(buildMProfileCase("M2_boundary_pipeline_buffer", "M2", "M2_s2_base_b", 8121L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "mid boundary pipeline buffer",
                "M1-single/M2-main-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_BASE_B, 1, 1, null, "MODERATE", MODE_ONE, 1, 1));
        specs.add(buildMProfileCase("M2_boundary_pipeline_buffer", "M2", "M2_s2_tight_a", 8122L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "mid boundary pipeline buffer",
                "M1-single/M2-main-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_TIGHT_A, 2, 1, null, "TIGHT", MODE_ONE, 1, 1));
        specs.add(buildMProfileCase("M2_boundary_pipeline_buffer", "M2", "M2_s2_tight_b", 8123L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "mid boundary pipeline buffer",
                "M1-single/M2-main-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_TIGHT_B, 2, 1, null, "TIGHT", MODE_ONE, 1, 1));
        specs.add(buildMProfileCase("M2_boundary_pipeline_buffer", "M2", "M2_s2_two_a", 8124L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "mid boundary pipeline buffer",
                "M1-single/M2-main-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_TWO_A, 1, 2, null, "MODERATE", MODE_TWO, 1, 2));
        specs.add(buildMProfileCase("M2_boundary_pipeline_buffer", "M2", "M2_s2_two_b", 8125L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "mid boundary pipeline buffer",
                "M1-single/M2-main-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_TWO_B, 1, 1, null, "MODERATE", MODE_TWO, 1, 2));
        specs.add(buildMProfileCase("M2_boundary_pipeline_buffer", "M2", "M2_s2_three", 8126L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "mid boundary pipeline buffer",
                "M1-single/M2-main-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_S2_THREE, 1, 2, null, "MODERATE", MODE_THREE, 1, 3));
        specs.add(buildMProfileCase("M2_boundary_pipeline_buffer", "M2", "M2_s4_two", 8127L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "mid boundary pipeline buffer",
                "M1-single/M2-main-pipeline-buffer", 2, 2, 2, 4,
                PROFILE_S4_TWO, 1, 2, null, "MODERATE", MODE_TWO, 1, 2));
        specs.add(buildMProfileCase("M2_boundary_pipeline_buffer", "M2", "M2_s4_three", 8128L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "mid boundary pipeline buffer",
                "M1-single/M2-main-pipeline-buffer", 2, 2, 2, 4,
                PROFILE_S4_THREE, 1, 2, null, "MODERATE", MODE_THREE, 1, 3));
        specs.add(buildMProfileCase("M2_boundary_pipeline_buffer", "M2", "M2_s6_stress", 8129L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "mid boundary pipeline buffer",
                "M1-single/M2-main-pipeline-buffer", 3, 3, 2, 6,
                PROFILE_S6_STRESS, 1, 1, null, "MODERATE", MODE_THREE, 1, 3));
        return specs;
    }

    private static List<RandomChannelSpec> buildM3Cases() {
        List<RandomChannelSpec> specs = new ArrayList<RandomChannelSpec>();
        specs.add(buildMProfileCase("M3_boundary_split_pipeline_buffer", "M3", "M3_s2_base_a", 9120L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "mid boundary split pipeline buffer",
                "M1-single/M2-main-split-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_BASE_A, 1, 1, firstSplitChannel(2), "MODERATE", MODE_ONE, 1, 1));
        specs.add(buildMProfileCase("M3_boundary_split_pipeline_buffer", "M3", "M3_s2_base_b", 9121L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "mid boundary split pipeline buffer",
                "M1-single/M2-main-split-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_BASE_B, 1, 1, firstSplitChannel(2), "MODERATE", MODE_ONE, 1, 1));
        specs.add(buildMProfileCase("M3_boundary_split_pipeline_buffer", "M3", "M3_s2_tight_a", 9122L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "mid boundary split pipeline buffer",
                "M1-single/M2-main-split-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_TIGHT_A, 1, 1, firstSplitChannel(2), "TIGHT", MODE_ONE, 1, 1));
        specs.add(buildMProfileCase("M3_boundary_split_pipeline_buffer", "M3", "M3_s2_tight_b", 9123L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "mid boundary split pipeline buffer",
                "M1-single/M2-main-split-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_TIGHT_B, 1, 1, firstSplitChannel(2), "TIGHT", MODE_ONE, 1, 1));
        specs.add(buildMProfileCase("M3_boundary_split_pipeline_buffer", "M3", "M3_s2_two_a", 9124L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "mid boundary split pipeline buffer",
                "M1-single/M2-main-split-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_TWO_A, 1, 2, firstSplitChannel(2), "MODERATE", MODE_TWO, 1, 2));
        specs.add(buildMProfileCase("M3_boundary_split_pipeline_buffer", "M3", "M3_s2_two_b", 9125L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "mid boundary split pipeline buffer",
                "M1-single/M2-main-split-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_TWO_B, 1, 2, firstSplitChannel(2), "MODERATE", MODE_TWO, 1, 2));
        specs.add(buildMProfileCase("M3_boundary_split_pipeline_buffer", "M3", "M3_s2_three", 9126L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "mid boundary split pipeline buffer",
                "M1-single/M2-main-split-pipeline-buffer", 2, 2, 2, 2,
                PROFILE_S2_THREE, 1, 2, firstSplitChannel(2), "MODERATE", MODE_THREE, 1, 3));
        specs.add(buildMProfileCase("M3_boundary_split_pipeline_buffer", "M3", "M3_s4_two", 9127L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "mid boundary split pipeline buffer",
                "M1-single/M2-main-split-pipeline-buffer", 2, 2, 2, 4,
                PROFILE_S4_TWO, 1, 2, firstSplitChannel(4), "MODERATE", MODE_TWO, 1, 2));
        specs.add(buildMProfileCase("M3_boundary_split_pipeline_buffer", "M3", "M3_s4_three", 9128L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "mid boundary split pipeline buffer",
                "M1-single/M2-main-split-pipeline-buffer", 2, 2, 2, 4,
                PROFILE_S4_THREE, 1, 2, firstSplitChannel(4), "MODERATE", MODE_THREE, 1, 3));
        specs.add(buildMProfileCase("M3_boundary_split_pipeline_buffer", "M3", "M3_s6_stress", 9129L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "mid boundary split pipeline buffer",
                "M1-single/M2-main-split-pipeline-buffer", 3, 3, 2, 6,
                PROFILE_S6_STRESS, 1, 1, firstSplitChannel(6), "MODERATE", MODE_THREE, 1, 3));
        return specs;
    }

    private static List<RandomChannelSpec> buildM4Cases() {
        List<RandomChannelSpec> specs = new ArrayList<RandomChannelSpec>();
        specs.add(buildM4Case("M4_nontrivial_learning", "M4", "M4_sched_q2r1", 10120L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "showcase nontrivial scheduler buffer",
                "M1-single/M2-showcase-scheduler-buffer", 1, 1, 2, 2,
                PROFILE_Q2_R1, SHOWCASE_Q2_R1, 1, 1, null, "TIGHT", MODE_TWO, 1, 2));
        specs.add(buildM4Case("M4_nontrivial_learning", "M4", "M4_sched_q3r1", 10121L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "showcase nontrivial scheduler buffer",
                "M1-single/M2-showcase-scheduler-buffer", 2, 2, 2, 4,
                PROFILE_Q3_R1, SHOWCASE_Q3_R1, 1, 1, null, "TIGHT", MODE_THREE, 1, 3));
        specs.add(buildM4Case("M4_nontrivial_learning", "M4", "M4_sched_q3r2", 10122L,
                TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER, "showcase nontrivial scheduler buffer",
                "M1-single/M2-showcase-scheduler-buffer", 2, 2, 2, 4,
                PROFILE_Q3_R2, SHOWCASE_Q3_R2, 2, 2, null, "TIGHT", MODE_THREE, 1, 3));

        specs.add(buildM4Case("M4_nontrivial_learning", "M4", "M4_pipe_q2r1", 10220L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "showcase nontrivial pipeline buffer",
                "M1-single/M2-showcase-pipeline-buffer", 1, 1, 2, 2,
                PROFILE_Q2_R1, SHOWCASE_Q2_R1, 0, 1, null, "TIGHT", MODE_TWO, 1, 2));
        specs.add(buildM4Case("M4_nontrivial_learning", "M4", "M4_pipe_q3r1", 10221L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "showcase nontrivial pipeline buffer",
                "M1-single/M2-showcase-pipeline-buffer", 2, 2, 2, 4,
                PROFILE_Q3_R1, SHOWCASE_Q3_R1, 0, 1, null, "TIGHT", MODE_THREE, 1, 3));
        specs.add(buildM4Case("M4_nontrivial_learning", "M4", "M4_pipe_q3r2", 10222L,
                TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER, "showcase nontrivial pipeline buffer",
                "M1-single/M2-showcase-pipeline-buffer", 2, 2, 2, 4,
                PROFILE_Q3_R2, SHOWCASE_Q3_R2, 1, 2, null, "TIGHT", MODE_THREE, 1, 3));

        specs.add(buildM4Case("M4_nontrivial_learning", "M4", "M4_split_q2r1", 10320L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "showcase nontrivial split pipeline buffer",
                "M1-single/M2-showcase-split-pipeline-buffer", 1, 1, 2, 2,
                PROFILE_Q2_R1, SHOWCASE_Q2_R1, 0, 1, firstSplitChannel(2), "TIGHT", MODE_TWO, 1, 2));
        specs.add(buildM4Case("M4_nontrivial_learning", "M4", "M4_split_q3r1", 10321L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "showcase nontrivial split pipeline buffer",
                "M1-single/M2-showcase-split-pipeline-buffer", 2, 2, 2, 4,
                PROFILE_Q3_R1, SHOWCASE_Q3_R1, 0, 1, firstSplitChannel(4), "TIGHT", MODE_THREE, 1, 3));
        specs.add(buildM4Case("M4_nontrivial_learning", "M4", "M4_split_q3r2", 10322L,
                TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER, "showcase nontrivial split pipeline buffer",
                "M1-single/M2-showcase-split-pipeline-buffer", 2, 2, 2, 4,
                PROFILE_Q3_R2, SHOWCASE_Q3_R2, 1, 2, firstSplitChannel(4), "TIGHT", MODE_THREE, 1, 3));
        return specs;
    }

    private static RandomChannelSpec buildPerformanceBufferCase(String suiteId, String family, int index, long seed,
                                                                String topologyKind, String description,
                                                                String structureGroup, int writerCount,
                                                                int readerCount, int len, int phaseDepth,
                                                                int dispatcherDepth,
                                                                String splitChannel) {
        RandomChannelSpec spec = baseSpec(suiteId, family, index, seed, PURPOSE_PERFORMANCE,
                topologyKind, "BUFFER", "BUF_BOUND", description, structureGroup,
                splitChannel == null ? "NONE" : "BIDIRECTIONAL_DOMAIN_SPLIT");
        spec.setWriterCount(writerCount);
        spec.setReaderCount(readerCount);
        spec.setPhaseDepth(phaseDepth);
        spec.setDispatcherDepth(dispatcherDepth);
        spec.setNearBoundaryMode("BOUNDARY_HIGH");
        spec.setSplitChannel(splitChannel);
        spec.setM1Spec(bufferMonitor("Buf", len, true, true));
        spec.setM2Spec(new ArrayList<RandomChannelSpec.ActorSpec>());
        spec.setChannelSpec(channelSpec(Arrays.asList("write", "read"),
                splitChannel == null ? Arrays.<String>asList() : Arrays.asList(splitChannel),
                Arrays.asList("write", "read")));
        spec.setTimingSpec(timeoutSpec());
        return spec;
    }

    private static RandomChannelSpec buildMainPerformanceCase(String suiteId, String family, String caseId, long seed,
                                                              String topologyKind, String description,
                                                              String structureGroup, int writerCount,
                                                              int readerCount, int len, int alphabetSize,
                                                              String variantId, int phaseDepth,
                                                              int dispatcherDepth, String splitChannel,
                                                              String nearBoundaryMode, String modePattern,
                                                              int burstLength, int modeCount) {
        RandomChannelSpec spec = baseSpec(suiteId, family, 0, seed, PURPOSE_PERFORMANCE,
                topologyKind, "BUFFER", "BUF_BOUND", description, structureGroup,
                splitChannel == null ? "NONE" : "BIDIRECTIONAL_DOMAIN_SPLIT");
        spec.setCaseId(caseId);
        spec.setWriterCount(writerCount);
        spec.setReaderCount(readerCount);
        spec.setAlphabetSize(alphabetSize);
        spec.setVariantId(variantId);
        spec.setPhaseDepth(phaseDepth);
        spec.setDispatcherDepth(dispatcherDepth);
        spec.setModePattern(modePattern);
        spec.setBurstLength(burstLength);
        spec.setModeCount(modeCount);
        spec.setNearBoundaryMode(nearBoundaryMode);
        spec.setSplitChannel(splitChannel);
        spec.setM1Spec(bufferMonitor("Buf", len, true, false));
        spec.setM2Spec(new ArrayList<RandomChannelSpec.ActorSpec>());
        List<String> sends = interfaceChannels(alphabetSize);
        spec.setChannelSpec(channelSpec(
                sends,
                splitChannel == null ? Arrays.<String>asList() : Arrays.asList(splitChannel),
                sends));
        spec.setTimingSpec(timeoutSpec());
        return spec;
    }

    private static RandomChannelSpec buildMProfileCase(String suiteId, String family, String caseId, long seed,
                                                       String topologyKind, String description,
                                                       String structureGroup, int writerCount,
                                                       int readerCount, int len, int alphabetSize,
                                                       String caseProfile, int phaseDepth,
                                                       int dispatcherDepth, String splitChannel,
                                                       String nearBoundaryMode, String modePattern,
                                                       int burstLength, int modeCount) {
        RandomChannelSpec spec = buildMainPerformanceCase(
                suiteId, family, caseId, seed, topologyKind, description, structureGroup,
                writerCount, readerCount, len, alphabetSize, "", phaseDepth, dispatcherDepth,
                splitChannel, nearBoundaryMode, modePattern, burstLength, modeCount);
        spec.setCaseProfile(caseProfile);
        boolean safeWrite = resolveMidSafeWrite(caseProfile, topologyKind);
        int monitorLen = resolveMidMonitorLen(caseProfile, len, topologyKind);
        spec.setM1Spec(bufferMonitor("Buf", monitorLen, true, safeWrite));
        return spec;
    }

    private static RandomChannelSpec buildM4Case(String suiteId, String family, String caseId, long seed,
                                                 String topologyKind, String description,
                                                 String structureGroup, int writerCount,
                                                 int readerCount, int len, int alphabetSize,
                                                 String caseProfile, String showcaseTarget,
                                                 int phaseDepth, int dispatcherDepth, String splitChannel,
                                                 String nearBoundaryMode, String modePattern,
                                                 int burstLength, int modeCount) {
        RandomChannelSpec spec = buildMainPerformanceCase(
                suiteId, family, caseId, seed, topologyKind, description, structureGroup,
                writerCount, readerCount, len, alphabetSize, "", phaseDepth, dispatcherDepth,
                splitChannel, nearBoundaryMode, modePattern, burstLength, modeCount);
        spec.setCaseProfile(caseProfile);
        spec.setShowcaseTarget(showcaseTarget);
        spec.setM1Spec(bufferMonitor("Buf",
                resolveM4MonitorLen(showcaseTarget, len, topologyKind),
                true,
                resolveM4SafeWrite(showcaseTarget, topologyKind)));
        return spec;
    }

    private static boolean resolveMidSafeWrite(String caseProfile, String topologyKind) {
        if (PROFILE_BASE_A.equals(caseProfile)
                || PROFILE_BASE_B.equals(caseProfile)
                || PROFILE_TIGHT_A.equals(caseProfile)
                || PROFILE_TIGHT_B.equals(caseProfile)
                || PROFILE_TWO_A.equals(caseProfile)
                || PROFILE_TWO_B.equals(caseProfile)
                || PROFILE_S2_THREE.equals(caseProfile)
                || PROFILE_S4_TWO.equals(caseProfile)
                || PROFILE_S4_THREE.equals(caseProfile)
                || PROFILE_S6_STRESS.equals(caseProfile)) {
            return true;
        }
        return false;
    }

    private static int resolveMidMonitorLen(String caseProfile, int len, String topologyKind) {
        if (PROFILE_S6_STRESS.equals(caseProfile)) {
            return len;
        }
        return len;
    }

    private static boolean resolveM4SafeWrite(String showcaseTarget, String topologyKind) {
        return false;
    }

    private static int resolveM4MonitorLen(String showcaseTarget, int len, String topologyKind) {
        if (SHOWCASE_Q3_R2.equals(showcaseTarget)) {
            return Math.max(1, len - 1);
        }
        return len;
    }

    private static RandomChannelSpec buildSmallNonNegativeCase(String suiteId, String family, int index,
                                                               long seed, Random random, boolean safe) {
        int len = 2 + random.nextInt(2);
        int writeAt = safe ? 1 + random.nextInt(2) : 4 + random.nextInt(2);
        int readAt = safe ? writeAt + 2 + random.nextInt(2) : 1 + random.nextInt(2);
        RandomChannelSpec spec = baseSpec(suiteId, family, index, seed, PURPOSE_SANITY,
                TOPOLOGY_SIMPLE_BUFFER, "BUFFER", "BUF_NONNEG",
                safe ? "small buffer nonnegative safe" : "small buffer nonnegative unsafe",
                "M1-single/M2-small-singleclock", "NONE");
        spec.setM1Spec(bufferMonitor("Buf", len, safe, true));
        spec.setM2Spec(new ArrayList<RandomChannelSpec.ActorSpec>(Arrays.asList(
                loopActor("Writer1", "WRITER", "write", writeAt, writeAt),
                loopActor("Reader1", "READER", "read", readAt, readAt)
        )));
        spec.setChannelSpec(channelSpec(Arrays.asList("write", "read"), Arrays.<String>asList(), Arrays.asList("write", "read")));
        spec.setTimingSpec(timeoutSpec());
        return spec;
    }

    private static RandomChannelSpec buildSmallBoundedCase(String suiteId, String family, int index,
                                                           long seed, Random random, boolean safe) {
        int len = 1;
        int firstWrite = 1 + random.nextInt(2);
        int secondWrite = firstWrite + 1;
        int readAt = secondWrite + 3 + random.nextInt(2);
        RandomChannelSpec spec = baseSpec(suiteId, family, index, seed, PURPOSE_SANITY,
                TOPOLOGY_SIMPLE_BUFFER, "BUFFER", "BUF_BOUND",
                safe ? "small buffer bound safe" : "small buffer bound unsafe",
                "M1-single/M2-small-singleclock", "NONE");
        spec.setM1Spec(bufferMonitor("Buf", len, true, safe));
        spec.setM2Spec(new ArrayList<RandomChannelSpec.ActorSpec>(Arrays.asList(
                loopActor("Writer1", "WRITER", "write", firstWrite, firstWrite),
                loopActor("Writer2", "WRITER", "write", secondWrite, secondWrite),
                loopActor("Reader1", "READER", "read", readAt, readAt)
        )));
        spec.setChannelSpec(channelSpec(Arrays.asList("write", "read"), Arrays.<String>asList(), Arrays.asList("write", "read")));
        spec.setTimingSpec(timeoutSpec());
        return spec;
    }

    private static RandomChannelSpec buildSmallMutexCase(String suiteId, String family, int index,
                                                         long seed, Random random, boolean safe) {
        int base = 1 + random.nextInt(2);
        RandomChannelSpec spec = baseSpec(suiteId, family, index, seed, PURPOSE_SANITY,
                TOPOLOGY_SIMPLE_MUTEX, "MUTEX", "MUTEX",
                safe ? "small mutex safe" : "small mutex unsafe",
                "M1-single/M2-small-singleclock", "NONE");
        spec.setM1Spec(mutexMonitor("Mutex", 2, safe));
        spec.setM2Spec(new ArrayList<RandomChannelSpec.ActorSpec>(Arrays.asList(
                clientActor("Client1", "enter1", "exit1", base, base, base + 4, base + 4),
                clientActor("Client2", "enter2", "exit2", base + 1, base + 1, base + 5, base + 5)
        )));
        spec.setChannelSpec(channelSpec(Arrays.asList("enter1", "exit1", "enter2", "exit2"),
                Arrays.<String>asList(),
                Arrays.asList("enter1", "exit1", "enter2", "exit2")));
        spec.setTimingSpec(timeoutSpec());
        return spec;
    }

    private static RandomChannelSpec buildLargeNonNegativeCase(String suiteId, String family, int index,
                                                               long seed, Random random, boolean safe) {
        int len = 3 + random.nextInt(2);
        int base = 1 + random.nextInt(2);
        RandomChannelSpec spec = baseSpec(suiteId, family, index, seed, PURPOSE_SANITY,
                TOPOLOGY_SIMPLE_BUFFER, "BUFFER", "BUF_NONNEG",
                safe ? "large buffer nonnegative safe" : "large buffer nonnegative unsafe",
                "M1-single/M2-multi-multiclock", "NONE");
        spec.setM1Spec(bufferMonitor("Buf", len, safe, true));
        List<RandomChannelSpec.ActorSpec> actors = new ArrayList<RandomChannelSpec.ActorSpec>();
        if (safe) {
            actors.add(loopActor("Writer1", "WRITER", "write", base, base));
            actors.add(loopActor("Writer2", "WRITER", "write", base + 1, base + 1));
            actors.add(loopActor("Reader1", "READER", "read", base + 4, base + 4));
            actors.add(loopActor("Reader2", "READER", "read", base + 5, base + 5));
        } else {
            actors.add(loopActor("Reader1", "READER", "read", base, base));
            actors.add(loopActor("Reader2", "READER", "read", base + 1, base + 1));
            actors.add(loopActor("Writer1", "WRITER", "write", base + 4, base + 4));
            actors.add(loopActor("Writer2", "WRITER", "write", base + 5, base + 5));
        }
        addNoiseWritersAndReaders(actors, base + 7);
        spec.setM2Spec(actors);
        spec.setChannelSpec(channelSpec(Arrays.asList("write", "read"), Arrays.<String>asList(), Arrays.asList("write", "read")));
        spec.setTimingSpec(timeoutSpec());
        return spec;
    }

    private static RandomChannelSpec buildLargeBoundedCase(String suiteId, String family, int index,
                                                           long seed, Random random, boolean safe) {
        int len = 2;
        int base = 1 + random.nextInt(2);
        RandomChannelSpec spec = baseSpec(suiteId, family, index, seed, PURPOSE_SANITY,
                TOPOLOGY_SIMPLE_BUFFER, "BUFFER", "BUF_BOUND",
                safe ? "large buffer bound safe" : "large buffer bound unsafe",
                "M1-single/M2-multi-multiclock", "NONE");
        spec.setM1Spec(bufferMonitor("Buf", len, true, safe));
        List<RandomChannelSpec.ActorSpec> actors = new ArrayList<RandomChannelSpec.ActorSpec>();
        actors.add(loopActor("Writer1", "WRITER", "write", base, base));
        actors.add(loopActor("Writer2", "WRITER", "write", base + 1, base + 1));
        actors.add(loopActor("Writer3", "WRITER", "write", base + 2, base + 2));
        actors.add(loopActor("Reader1", "READER", "read", base + 6, base + 6));
        actors.add(loopActor("Reader2", "READER", "read", base + 7, base + 7));
        spec.setM2Spec(actors);
        spec.setChannelSpec(channelSpec(Arrays.asList("write", "read"), Arrays.<String>asList(), Arrays.asList("write", "read")));
        spec.setTimingSpec(timeoutSpec());
        return spec;
    }

    private static RandomChannelSpec buildLargeMutexCase(String suiteId, String family, int index,
                                                         long seed, Random random, boolean safe) {
        int base = 1 + random.nextInt(2);
        RandomChannelSpec spec = baseSpec(suiteId, family, index, seed, PURPOSE_SANITY,
                TOPOLOGY_SIMPLE_MUTEX, "MUTEX", "MUTEX",
                safe ? "large mutex safe" : "large mutex unsafe",
                "M1-single/M2-multi-multiclock", "NONE");
        spec.setM1Spec(mutexMonitor("Mutex", 4, safe));
        spec.setM2Spec(new ArrayList<RandomChannelSpec.ActorSpec>(Arrays.asList(
                clientActor("Client1", "enter1", "exit1", base, base, base + 5, base + 5),
                clientActor("Client2", "enter2", "exit2", base + 1, base + 1, base + 6, base + 6),
                clientActor("Client3", "enter3", "exit3", base + 2, base + 2, base + 7, base + 7),
                clientActor("Client4", "enter4", "exit4", base + 3, base + 3, base + 8, base + 8)
        )));
        spec.setChannelSpec(channelSpec(Arrays.asList("enter1", "exit1", "enter2", "exit2", "enter3", "exit3", "enter4", "exit4"),
                Arrays.<String>asList(),
                Arrays.asList("enter1", "exit1", "enter2", "exit2", "enter3", "exit3", "enter4", "exit4")));
        spec.setTimingSpec(timeoutSpec());
        return spec;
    }

    private static RandomChannelSpec buildSplitNonNegativeCase(String suiteId, String family, int index,
                                                               long seed, Random random, boolean safe) {
        int len = 2 + random.nextInt(2);
        int writeAt = safe ? 1 : 4;
        int readAt = safe ? 3 : 1;
        int innerAt = 6 + random.nextInt(2);
        RandomChannelSpec spec = baseSpec(suiteId, family, index, seed, PURPOSE_SANITY,
                TOPOLOGY_SPLIT_BUFFER, "BUFFER", "BUF_NONNEG",
                safe ? "split buffer nonnegative safe" : "split buffer nonnegative unsafe",
                "M1-single/M2-conflict-multiclock", "BIDIRECTIONAL_DOMAIN_SPLIT");
        spec.setM1Spec(bufferMonitor("Buf", len, safe, true));
        spec.setM2Spec(new ArrayList<RandomChannelSpec.ActorSpec>(Arrays.asList(
                loopActor("WriterExt", "WRITER", "write", writeAt, writeAt),
                loopActor("ReaderExt", "READER", "read", readAt, readAt),
                loopActor("InnerReadSend", "INTERNAL_SENDER", "read", innerAt, innerAt),
                loopActor("InnerReadRecv", "INTERNAL_RECEIVER", "read", innerAt, innerAt)
        )));
        spec.setChannelSpec(channelSpec(Arrays.asList("write", "read"), Arrays.asList("read"), Arrays.asList("write", "read")));
        spec.setTimingSpec(timeoutSpec());
        return spec;
    }

    private static RandomChannelSpec buildSplitBoundedCase(String suiteId, String family, int index,
                                                           long seed, Random random, boolean safe) {
        int len = 1;
        int firstWrite = 1 + random.nextInt(2);
        int innerWrite = firstWrite + 1;
        int readAt = innerWrite + 5;
        RandomChannelSpec spec = baseSpec(suiteId, family, index, seed, PURPOSE_SANITY,
                TOPOLOGY_SPLIT_BUFFER, "BUFFER", "BUF_BOUND",
                safe ? "split buffer bound safe" : "split buffer bound unsafe",
                "M1-single/M2-conflict-multiclock", "BIDIRECTIONAL_DOMAIN_SPLIT");
        spec.setM1Spec(bufferMonitor("Buf", len, true, safe));
        spec.setM2Spec(new ArrayList<RandomChannelSpec.ActorSpec>(Arrays.asList(
                loopActor("WriterExt", "WRITER", "write", firstWrite, firstWrite),
                loopActor("ReaderExt", "READER", "read", readAt, readAt),
                loopActor("InnerWriteSend", "INTERNAL_SENDER", "write", innerWrite, innerWrite),
                loopActor("InnerWriteRecv", "INTERNAL_RECEIVER", "write", innerWrite, innerWrite)
        )));
        spec.setChannelSpec(channelSpec(Arrays.asList("write", "read"), Arrays.asList("write"), Arrays.asList("write", "read")));
        spec.setTimingSpec(timeoutSpec());
        return spec;
    }

    private static void addNoiseWritersAndReaders(List<RandomChannelSpec.ActorSpec> actors, int base) {
        actors.add(loopActor("WriterN", "WRITER", "write", base, base));
        actors.add(loopActor("ReaderN", "READER", "read", base + 2, base + 2));
    }

    private static RandomChannelSpec baseSpec(String suiteId, String family, int index, long seed,
                                              String suitePurpose, String topologyKind,
                                              String modelKind, String propertyId, String description,
                                              String structureGroup, String preprocess) {
        RandomChannelSpec spec = new RandomChannelSpec();
        spec.setSuiteId(suiteId);
        spec.setCaseId(family + "_" + String.format("%02d", index));
        spec.setSeed(seed);
        spec.setFamily(family);
        spec.setSuitePurpose(suitePurpose);
        spec.setTopologyKind(topologyKind);
        spec.setModelKind(modelKind);
        spec.setPropertyId(propertyId);
        spec.setDescription(description);
        spec.setStructureGroup(structureGroup);
        spec.setAlphabetSize(0);
        spec.setVariantId("");
        spec.setCaseProfile("");
        spec.setModePattern("");
        spec.setBurstLength(0);
        spec.setModeCount(0);
        spec.setExpectedPreprocess(preprocess);
        return spec;
    }

    private static List<String> interfaceChannels(int alphabetSize) {
        List<String> channels = new ArrayList<String>();
        int perRole = Math.max(1, alphabetSize / 2);
        for (int i = 1; i <= perRole; i++) {
            channels.add("write" + i);
        }
        for (int i = 1; i <= perRole; i++) {
            channels.add("read" + i);
        }
        return channels;
    }

    private static String firstSplitChannel(int alphabetSize) {
        int perRole = Math.max(1, alphabetSize / 2);
        return perRole == 1 ? "read1" : "read1";
    }

    private static RandomChannelSpec.MonitorSpec bufferMonitor(String name, int len, boolean safeRead, boolean safeWrite) {
        RandomChannelSpec.MonitorSpec spec = new RandomChannelSpec.MonitorSpec();
        spec.setName(name);
        spec.setType("BUFFER");
        spec.setLen(len);
        spec.setSafeRead(safeRead);
        spec.setSafeWrite(safeWrite);
        spec.setClientCount(0);
        return spec;
    }

    private static RandomChannelSpec.MonitorSpec mutexMonitor(String name, int clients, boolean safeMutex) {
        RandomChannelSpec.MonitorSpec spec = new RandomChannelSpec.MonitorSpec();
        spec.setName(name);
        spec.setType("MUTEX");
        spec.setLen(0);
        spec.setSafeRead(false);
        spec.setSafeWrite(false);
        spec.setSafeMutex(safeMutex);
        spec.setClientCount(clients);
        return spec;
    }

    private static RandomChannelSpec.ActorSpec loopActor(String name, String kind, String channel, int low, int high) {
        RandomChannelSpec.ActorSpec actor = new RandomChannelSpec.ActorSpec();
        actor.setName(name);
        actor.setKind(kind);
        actor.setChannel(channel);
        actor.setLow(low);
        actor.setHigh(high);
        actor.setSecondLow(0);
        actor.setSecondHigh(0);
        return actor;
    }

    private static RandomChannelSpec.ActorSpec clientActor(String name, String enter, String exit,
                                                           int enterLow, int enterHigh,
                                                           int exitLow, int exitHigh) {
        RandomChannelSpec.ActorSpec actor = new RandomChannelSpec.ActorSpec();
        actor.setName(name);
        actor.setKind("CLIENT");
        actor.setEnterChannel(enter);
        actor.setExitChannel(exit);
        actor.setLow(enterLow);
        actor.setHigh(enterHigh);
        actor.setSecondLow(exitLow);
        actor.setSecondHigh(exitHigh);
        return actor;
    }

    private static RandomChannelSpec.ChannelSpec channelSpec(List<String> sends, List<String> splitChannels, List<String> resetSeeds) {
        RandomChannelSpec.ChannelSpec spec = new RandomChannelSpec.ChannelSpec();
        spec.setSendChannels(new ArrayList<String>(sends));
        spec.setSplitChannels(new ArrayList<String>(splitChannels));
        spec.setResetSeedChannels(new ArrayList<String>(resetSeeds));
        return spec;
    }

    private static RandomChannelSpec.TimingSpec timeoutSpec() {
        RandomChannelSpec.TimingSpec timing = new RandomChannelSpec.TimingSpec();
        timing.setDirectTimeoutMs(DEFAULT_TIMEOUT_MS);
        timing.setAgTimeoutMs(DEFAULT_TIMEOUT_MS);
        return timing;
    }

    private static void writeJson(File file, Object obj) throws IOException {
        String json = JSON.toJSONString(obj, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue);
        Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
    }

    private static void ensureDir(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Failed to create directory: " + dir.getAbsolutePath());
        }
    }

    private static void resetDir(File dir) throws IOException {
        if (dir.exists()) {
            deleteRecursively(dir);
        }
        ensureDir(dir);
    }

    private static void deleteRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        Files.deleteIfExists(file.toPath());
    }

    private static void ensureResultDirs(File root) {
        ensureDir(root);
        ensureDir(new File(root, "direct"));
        ensureDir(new File(root, "ag_static"));
        ensureDir(new File(root, "ag_dynamic"));
        ensureDir(new File(root, "summary"));
    }

    private static String join(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append("+");
            }
            sb.append(values.get(i));
        }
        return sb.toString();
    }
}
