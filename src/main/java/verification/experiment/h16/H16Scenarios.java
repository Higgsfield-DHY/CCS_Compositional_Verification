package verification.experiment.h16;

import verification.report.AgVerdict;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class H16Scenarios {
    private H16Scenarios() {
    }

    public static H16ScenarioConfig c1BaseSafe() {
        return new H16ScenarioConfig("C1_BASE_SAFE")
                .setExpectedVerdict(AgVerdict.SAFE)
                .setMinCq1Fails(1);
    }

    public static H16ScenarioConfig c2SafeRenameInternal() {
        Map<String, String> renameMap = new HashMap<String, String>();
        renameMap.put("H5:a", "a_int");
        renameMap.put("H6:a", "a_int");
        return new H16ScenarioConfig("C2_SAFE_RENAME_INTERNAL")
                .setH5Channel("a")
                .setH6Channel("a")
                .setM2RenameMap(renameMap)
                .setExpectedVerdict(AgVerdict.SAFE)
                .setMinCq1Fails(1);
    }

    public static H16ScenarioConfig c3SafeEdgeInteger() {
        return new H16ScenarioConfig("C3_SAFE_EDGE_INTEGER")
                .setH3Guard("x>=1 && x<=1")
                .setExpectedVerdict(AgVerdict.SAFE)
                .setMinCq1Fails(1);
    }

    public static H16ScenarioConfig c4SafeNoBSync() {
        return new H16ScenarioConfig("C4_SAFE_NO_B_SYNC")
                .setH4Guard("x>3 && x<=4")
                .setExpectedVerdict(AgVerdict.SAFE);
    }

    public static H16ScenarioConfig c5UnsafeBThenA() {
        return new H16ScenarioConfig("C5_UNSAFE_B_THEN_A")
                .setH3Guard("x>=2 && x<=3")
                .setExpectedVerdict(AgVerdict.UNSAFE)
                .setMinCq1Fails(1);
    }

    public static H16ScenarioConfig c6UnsafeDelayedA() {
        return new H16ScenarioConfig("C6_UNSAFE_DELAYED_A")
                .setH3Guard("x>=3 && x<=4")
                .setExpectedVerdict(AgVerdict.UNSAFE)
                .setMinCq1Fails(1);
    }

    public static List<H16ScenarioConfig> learningSuite() {
        return Arrays.asList(
                c1BaseSafe(),
                c2SafeRenameInternal(),
                c3SafeEdgeInteger(),
                c4SafeNoBSync(),
                c5UnsafeBThenA(),
                c6UnsafeDelayedA()
        );
    }
}
