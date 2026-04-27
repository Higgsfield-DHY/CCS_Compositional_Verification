package verification.experiment.random_channel;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class RandomChannelCases {
    public static final String ROOT_DIR = "Experiments/RandChannelCases";
    public static final String G_RESULT_DIR = ROOT_DIR + "/G_exp_result";
    public static final String P_RESULT_DIR = ROOT_DIR + "/P_exp_result";
    public static final String M_RESULT_DIR = ROOT_DIR + "/M_exp_result";

    private RandomChannelCases() {
    }

    public enum SuiteGroup {
        G1_CLEAN_SMALL("G1_clean_small"),
        G2_CLEAN_MULTI("G2_clean_multi"),
        G3_SPLIT_REQUIRED("G3_split_required"),
        P1_BOUNDARY_SCHED_BUFFER("P1_boundary_sched_buffer"),
        P2_BOUNDARY_PIPELINE_BUFFER("P2_boundary_pipeline_buffer"),
        P3_BOUNDARY_SPLIT_PIPELINE_BUFFER("P3_boundary_split_pipeline_buffer"),
        M1_BOUNDARY_SCHED_BUFFER("M1_boundary_sched_buffer"),
        M2_BOUNDARY_PIPELINE_BUFFER("M2_boundary_pipeline_buffer"),
        M3_BOUNDARY_SPLIT_PIPELINE_BUFFER("M3_boundary_split_pipeline_buffer"),
        M4_NONTRIVIAL_LEARNING("M4_nontrivial_learning"),
        SANITY_ALL(null),
        PERF_ALL(null),
        M_ALL(null),
        M_LEARNING_ALL(null),
        ALL(null);

        private final String suiteDir;

        SuiteGroup(String suiteDir) {
            this.suiteDir = suiteDir;
        }

        public String getSuiteDir() {
            return suiteDir;
        }

        public boolean isSyntheticGroup() {
            return this == SANITY_ALL || this == PERF_ALL || this == M_ALL || this == M_LEARNING_ALL || this == ALL;
        }
    }

    public static List<RandomChannelCase> loadCases(SuiteGroup group) throws IOException {
        List<RandomChannelCase> result = new ArrayList<RandomChannelCase>();
        if (group == SuiteGroup.ALL) {
            for (SuiteGroup value : SuiteGroup.values()) {
                if (!value.isSyntheticGroup()) {
                    result.addAll(loadSuite(value.getSuiteDir()));
                }
            }
        } else if (group == SuiteGroup.SANITY_ALL) {
            result.addAll(loadSuite(SuiteGroup.G1_CLEAN_SMALL.getSuiteDir()));
            result.addAll(loadSuite(SuiteGroup.G2_CLEAN_MULTI.getSuiteDir()));
            result.addAll(loadSuite(SuiteGroup.G3_SPLIT_REQUIRED.getSuiteDir()));
        } else if (group == SuiteGroup.PERF_ALL) {
            result.addAll(loadSuite(SuiteGroup.P1_BOUNDARY_SCHED_BUFFER.getSuiteDir()));
            result.addAll(loadSuite(SuiteGroup.P2_BOUNDARY_PIPELINE_BUFFER.getSuiteDir()));
            result.addAll(loadSuite(SuiteGroup.P3_BOUNDARY_SPLIT_PIPELINE_BUFFER.getSuiteDir()));
        } else if (group == SuiteGroup.M_ALL) {
            result.addAll(loadSuite(SuiteGroup.M1_BOUNDARY_SCHED_BUFFER.getSuiteDir()));
            result.addAll(loadSuite(SuiteGroup.M2_BOUNDARY_PIPELINE_BUFFER.getSuiteDir()));
            result.addAll(loadSuite(SuiteGroup.M3_BOUNDARY_SPLIT_PIPELINE_BUFFER.getSuiteDir()));
        } else if (group == SuiteGroup.M_LEARNING_ALL) {
            result.addAll(loadSuite(SuiteGroup.M4_NONTRIVIAL_LEARNING.getSuiteDir()));
        } else {
            result.addAll(loadSuite(group.getSuiteDir()));
        }
        Collections.sort(result, new Comparator<RandomChannelCase>() {
            @Override
            public int compare(RandomChannelCase left, RandomChannelCase right) {
                int suite = left.getSuiteId().compareTo(right.getSuiteId());
                if (suite != 0) {
                    return suite;
                }
                return left.getCaseId().compareTo(right.getCaseId());
            }
        });
        return result;
    }

    public static RandomChannelCase findCase(SuiteGroup group, String caseId) throws IOException {
        if (caseId == null) {
            return null;
        }
        String key = caseId.trim().toLowerCase(Locale.ROOT);
        for (RandomChannelCase c : loadCases(group)) {
            if (c.normalizedKey().equals(key)) {
                return c;
            }
        }
        return null;
    }

    private static List<RandomChannelCase> loadSuite(String suiteDir) throws IOException {
        File suiteRoot = new File(ROOT_DIR, suiteDir);
        if (!suiteRoot.isDirectory()) {
            throw new IllegalStateException("Random suite directory not found: " + suiteRoot.getAbsolutePath());
        }
        File[] caseDirs = suiteRoot.listFiles();
        List<RandomChannelCase> result = new ArrayList<RandomChannelCase>();
        if (caseDirs == null) {
            return result;
        }
        for (File caseDir : caseDirs) {
            if (!caseDir.isDirectory()) {
                continue;
            }
            File specFile = new File(caseDir, "spec.json");
            File manifestFile = new File(caseDir, "manifest.json");
            if (!specFile.isFile() || !manifestFile.isFile()) {
                continue;
            }
            RandomChannelSpec spec = JSON.parseObject(readUtf8(specFile), RandomChannelSpec.class);
            RandomChannelManifest manifest = JSON.parseObject(readUtf8(manifestFile), RandomChannelManifest.class);
            result.add(new RandomChannelCase(caseDir, spec, manifest));
        }
        return result;
    }

    private static String readUtf8(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
