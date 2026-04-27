package verification.experiment.random_channel;

import java.util.ArrayList;
import java.util.List;

public class RandomChannelSpec {
    private String suiteId;
    private String caseId;
    private long seed;
    private String family;
    private String suitePurpose;
    private String topologyKind;
    private String modelKind;
    private String propertyId;
    private String description;
    private String structureGroup;
    private int alphabetSize;
    private String variantId;
    private String caseProfile;
    private String showcaseTarget;
    private int writerCount;
    private int readerCount;
    private int phaseDepth;
    private int dispatcherDepth;
    private String modePattern;
    private int burstLength;
    private int modeCount;
    private String nearBoundaryMode;
    private String splitChannel;
    private MonitorSpec m1Spec;
    private List<ActorSpec> m2Spec = new ArrayList<ActorSpec>();
    private ChannelSpec channelSpec;
    private TimingSpec timingSpec;
    private String expectedPreprocess;

    public String getSuiteId() {
        return suiteId;
    }

    public void setSuiteId(String suiteId) {
        this.suiteId = suiteId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getSuitePurpose() {
        return suitePurpose;
    }

    public void setSuitePurpose(String suitePurpose) {
        this.suitePurpose = suitePurpose;
    }

    public String getTopologyKind() {
        return topologyKind;
    }

    public void setTopologyKind(String topologyKind) {
        this.topologyKind = topologyKind;
    }

    public String getModelKind() {
        return modelKind;
    }

    public void setModelKind(String modelKind) {
        this.modelKind = modelKind;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStructureGroup() {
        return structureGroup;
    }

    public void setStructureGroup(String structureGroup) {
        this.structureGroup = structureGroup;
    }

    public int getAlphabetSize() {
        return alphabetSize;
    }

    public void setAlphabetSize(int alphabetSize) {
        this.alphabetSize = alphabetSize;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public String getCaseProfile() {
        return caseProfile;
    }

    public void setCaseProfile(String caseProfile) {
        this.caseProfile = caseProfile;
    }

    public String getShowcaseTarget() {
        return showcaseTarget;
    }

    public void setShowcaseTarget(String showcaseTarget) {
        this.showcaseTarget = showcaseTarget;
    }

    public int getWriterCount() {
        return writerCount;
    }

    public void setWriterCount(int writerCount) {
        this.writerCount = writerCount;
    }

    public int getReaderCount() {
        return readerCount;
    }

    public void setReaderCount(int readerCount) {
        this.readerCount = readerCount;
    }

    public int getPhaseDepth() {
        return phaseDepth;
    }

    public void setPhaseDepth(int phaseDepth) {
        this.phaseDepth = phaseDepth;
    }

    public int getDispatcherDepth() {
        return dispatcherDepth;
    }

    public void setDispatcherDepth(int dispatcherDepth) {
        this.dispatcherDepth = dispatcherDepth;
    }

    public String getModePattern() {
        return modePattern;
    }

    public void setModePattern(String modePattern) {
        this.modePattern = modePattern;
    }

    public int getBurstLength() {
        return burstLength;
    }

    public void setBurstLength(int burstLength) {
        this.burstLength = burstLength;
    }

    public int getModeCount() {
        return modeCount;
    }

    public void setModeCount(int modeCount) {
        this.modeCount = modeCount;
    }

    public String getNearBoundaryMode() {
        return nearBoundaryMode;
    }

    public void setNearBoundaryMode(String nearBoundaryMode) {
        this.nearBoundaryMode = nearBoundaryMode;
    }

    public String getSplitChannel() {
        return splitChannel;
    }

    public void setSplitChannel(String splitChannel) {
        this.splitChannel = splitChannel;
    }

    public MonitorSpec getM1Spec() {
        return m1Spec;
    }

    public void setM1Spec(MonitorSpec m1Spec) {
        this.m1Spec = m1Spec;
    }

    public List<ActorSpec> getM2Spec() {
        return m2Spec;
    }

    public void setM2Spec(List<ActorSpec> m2Spec) {
        this.m2Spec = m2Spec;
    }

    public ChannelSpec getChannelSpec() {
        return channelSpec;
    }

    public void setChannelSpec(ChannelSpec channelSpec) {
        this.channelSpec = channelSpec;
    }

    public TimingSpec getTimingSpec() {
        return timingSpec;
    }

    public void setTimingSpec(TimingSpec timingSpec) {
        this.timingSpec = timingSpec;
    }

    public String getExpectedPreprocess() {
        return expectedPreprocess;
    }

    public void setExpectedPreprocess(String expectedPreprocess) {
        this.expectedPreprocess = expectedPreprocess;
    }

    public static class MonitorSpec {
        private String name;
        private String type;
        private int len;
        private boolean safeRead;
        private boolean safeWrite;
        private boolean safeMutex;
        private int clientCount;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getLen() {
            return len;
        }

        public void setLen(int len) {
            this.len = len;
        }

        public boolean isSafeRead() {
            return safeRead;
        }

        public void setSafeRead(boolean safeRead) {
            this.safeRead = safeRead;
        }

        public boolean isSafeWrite() {
            return safeWrite;
        }

        public void setSafeWrite(boolean safeWrite) {
            this.safeWrite = safeWrite;
        }

        public boolean isSafeMutex() {
            return safeMutex;
        }

        public void setSafeMutex(boolean safeMutex) {
            this.safeMutex = safeMutex;
        }

        public int getClientCount() {
            return clientCount;
        }

        public void setClientCount(int clientCount) {
            this.clientCount = clientCount;
        }
    }

    public static class ActorSpec {
        private String kind;
        private String name;
        private String channel;
        private String enterChannel;
        private String exitChannel;
        private int low;
        private int high;
        private int secondLow;
        private int secondHigh;

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getEnterChannel() {
            return enterChannel;
        }

        public void setEnterChannel(String enterChannel) {
            this.enterChannel = enterChannel;
        }

        public String getExitChannel() {
            return exitChannel;
        }

        public void setExitChannel(String exitChannel) {
            this.exitChannel = exitChannel;
        }

        public int getLow() {
            return low;
        }

        public void setLow(int low) {
            this.low = low;
        }

        public int getHigh() {
            return high;
        }

        public void setHigh(int high) {
            this.high = high;
        }

        public int getSecondLow() {
            return secondLow;
        }

        public void setSecondLow(int secondLow) {
            this.secondLow = secondLow;
        }

        public int getSecondHigh() {
            return secondHigh;
        }

        public void setSecondHigh(int secondHigh) {
            this.secondHigh = secondHigh;
        }
    }

    public static class ChannelSpec {
        private List<String> sendChannels = new ArrayList<String>();
        private List<String> splitChannels = new ArrayList<String>();
        private List<String> resetSeedChannels = new ArrayList<String>();

        public List<String> getSendChannels() {
            return sendChannels;
        }

        public void setSendChannels(List<String> sendChannels) {
            this.sendChannels = sendChannels;
        }

        public List<String> getSplitChannels() {
            return splitChannels;
        }

        public void setSplitChannels(List<String> splitChannels) {
            this.splitChannels = splitChannels;
        }

        public List<String> getResetSeedChannels() {
            return resetSeedChannels;
        }

        public void setResetSeedChannels(List<String> resetSeedChannels) {
            this.resetSeedChannels = resetSeedChannels;
        }
    }

    public static class TimingSpec {
        private int directTimeoutMs;
        private int agTimeoutMs;

        public int getDirectTimeoutMs() {
            return directTimeoutMs;
        }

        public void setDirectTimeoutMs(int directTimeoutMs) {
            this.directTimeoutMs = directTimeoutMs;
        }

        public int getAgTimeoutMs() {
            return agTimeoutMs;
        }

        public void setAgTimeoutMs(int agTimeoutMs) {
            this.agTimeoutMs = agTimeoutMs;
        }
    }
}
