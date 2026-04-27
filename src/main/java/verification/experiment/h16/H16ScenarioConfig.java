package verification.experiment.h16;

import verification.report.AgVerdict;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class H16ScenarioConfig {
    private final String caseName;
    private String h3Guard = "x>=1 && x<=2";
    private String h4Guard = "x>2 && x<=3";
    private String h5Channel = "a";
    private String h6Channel = "a";
    private String h5Guard = "x>=1 && x<=2";
    private String h6Guard = "x>=1 && x<=2";
    private Map<String, String> m2RenameMap = Collections.emptyMap();
    private AgVerdict expectedVerdict = AgVerdict.SAFE;
    private int minCq1Fails;
    private int minCq2Fails;

    public H16ScenarioConfig(String caseName) {
        this.caseName = caseName;
    }

    public String getCaseName() {
        return caseName;
    }

    public String getH3Guard() {
        return h3Guard;
    }

    public H16ScenarioConfig setH3Guard(String h3Guard) {
        this.h3Guard = h3Guard;
        return this;
    }

    public String getH4Guard() {
        return h4Guard;
    }

    public H16ScenarioConfig setH4Guard(String h4Guard) {
        this.h4Guard = h4Guard;
        return this;
    }

    public String getH5Channel() {
        return h5Channel;
    }

    public H16ScenarioConfig setH5Channel(String h5Channel) {
        this.h5Channel = h5Channel;
        return this;
    }

    public String getH6Channel() {
        return h6Channel;
    }

    public H16ScenarioConfig setH6Channel(String h6Channel) {
        this.h6Channel = h6Channel;
        return this;
    }

    public String getH5Guard() {
        return h5Guard;
    }

    public H16ScenarioConfig setH5Guard(String h5Guard) {
        this.h5Guard = h5Guard;
        return this;
    }

    public String getH6Guard() {
        return h6Guard;
    }

    public H16ScenarioConfig setH6Guard(String h6Guard) {
        this.h6Guard = h6Guard;
        return this;
    }

    public Map<String, String> getM2RenameMap() {
        return m2RenameMap;
    }

    public H16ScenarioConfig setM2RenameMap(Map<String, String> m2RenameMap) {
        this.m2RenameMap = Collections.unmodifiableMap(new HashMap<>(m2RenameMap));
        return this;
    }

    public AgVerdict getExpectedVerdict() {
        return expectedVerdict;
    }

    public H16ScenarioConfig setExpectedVerdict(AgVerdict expectedVerdict) {
        this.expectedVerdict = expectedVerdict;
        return this;
    }

    public int getMinCq1Fails() {
        return minCq1Fails;
    }

    public H16ScenarioConfig setMinCq1Fails(int minCq1Fails) {
        this.minCq1Fails = minCq1Fails;
        return this;
    }

    public int getMinCq2Fails() {
        return minCq2Fails;
    }

    public H16ScenarioConfig setMinCq2Fails(int minCq2Fails) {
        this.minCq2Fails = minCq2Fails;
        return this;
    }
}
