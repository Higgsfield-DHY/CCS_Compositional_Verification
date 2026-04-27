package verification.experiment.random_channel;

import verification.experiment.Experiment;
import verification.plugins.SequenceChecker;
import verification.reset.ResetPolicyType;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.uppaal.model.UppaalLocation;
import verification.uppaal.model.UppaalTransition;
import verification.uppaal.model.builder.TemplateBuilder;
import verification.uppaal.model.builder.UppaalTransitionBuilder;
import verification.util.PortPreprocessConfig;
import verification.util.UppaalModelUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RandomChannelExperiment extends Experiment {
    private static final String MODEL_BUFFER = "BUFFER";
    private static final String MODEL_MUTEX = "MUTEX";

    private static final String TOPOLOGY_SIMPLE_BUFFER = "SIMPLE_BUFFER";
    private static final String TOPOLOGY_SIMPLE_MUTEX = "SIMPLE_MUTEX";
    private static final String TOPOLOGY_SPLIT_BUFFER = "SPLIT_BUFFER";
    private static final String TOPOLOGY_BOUNDARY_SCHED_BUFFER = "BOUNDARY_SCHED_BUFFER";
    private static final String TOPOLOGY_BOUNDARY_PIPELINE_BUFFER = "BOUNDARY_PIPELINE_BUFFER";
    private static final String TOPOLOGY_BOUNDARY_SPLIT_PIPELINE_BUFFER = "BOUNDARY_SPLIT_PIPELINE_BUFFER";
    private static final String TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER = "MID_BOUNDARY_SCHED_BUFFER";
    private static final String TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER = "MID_BOUNDARY_PIPELINE_BUFFER";
    private static final String TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER = "MID_BOUNDARY_SPLIT_PIPELINE_BUFFER";
    private static final String MODE_FILL = "FILL";
    private static final String MODE_DRAIN = "DRAIN";
    private static final String MODE_MIX = "MIX";
    private static final String MODE_ONE = "ONE_MODE";
    private static final String MODE_TWO = "TWO_MODE";
    private static final String MODE_THREE = "THREE_MODE";
    private static final String PROFILE_BASE = "BASE";
    private static final String PROFILE_TIGHT = "TIGHT";
    private static final String PROFILE_TWO_MODE = "TWO_MODE";
    private static final String PROFILE_THREE_MODE = "THREE_MODE";
    private static final String PROFILE_S6_STRESS = "S6_STRESS";

    private static final String ACTOR_WRITER = "WRITER";
    private static final String ACTOR_READER = "READER";
    private static final String ACTOR_CLIENT = "CLIENT";
    private static final String ACTOR_INTERNAL_SENDER = "INTERNAL_SENDER";
    private static final String ACTOR_INTERNAL_RECEIVER = "INTERNAL_RECEIVER";

    private final File caseDir;
    private final RandomChannelSpec spec;

    public RandomChannelExperiment(File caseDir, RandomChannelSpec spec) {
        this.caseDir = caseDir;
        this.spec = spec;
    }

    @Override
    public String getStatement() {
        String monitor = spec.getM1Spec().getName();
        if ("BUF_NONNEG".equals(spec.getPropertyId())) {
            return "A[] " + monitor + ".count >= 0";
        }
        if ("BUF_BOUND".equals(spec.getPropertyId())) {
            return "A[] " + monitor + ".count <= " + monitor + ".len";
        }
        if ("MUTEX".equals(spec.getPropertyId())) {
            return "A[] " + monitor + ".hold <= 1";
        }
        throw new IllegalStateException("Unsupported propertyId: " + spec.getPropertyId());
    }

    @Override
    public Map<String, Boolean> getSyncSendMap() {
        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
        for (String channel : spec.getChannelSpec().getSendChannels()) {
            map.put(channel, true);
        }
        return map;
    }

    @Override
    public Set<String> getResetSigma() {
        LinkedHashSet<String> sigma = new LinkedHashSet<String>();
        List<String> seeds = spec.getChannelSpec().getResetSeedChannels();
        if (seeds == null || seeds.isEmpty()) {
            seeds = spec.getChannelSpec().getSendChannels();
        }
        for (String channel : seeds) {
            sigma.add(toTargetAction(channel));
        }
        return sigma;
    }

    @Override
    public List<Template> getM1() {
        List<Template> list = new ArrayList<Template>();
        if (MODEL_BUFFER.equals(spec.getModelKind())) {
            list.add(buildBufferMonitor());
        } else if (MODEL_MUTEX.equals(spec.getModelKind())) {
            list.add(buildMutexMonitor());
        } else {
            throw new IllegalStateException("Unsupported modelKind: " + spec.getModelKind());
        }
        return list;
    }

    @Override
    public List<Template> getM2() {
        if (TOPOLOGY_BOUNDARY_SCHED_BUFFER.equals(spec.getTopologyKind())) {
            return buildSchedBufferM2();
        }
        if (TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER.equals(spec.getTopologyKind())) {
            return buildSchedBufferM2();
        }
        if (TOPOLOGY_BOUNDARY_PIPELINE_BUFFER.equals(spec.getTopologyKind())) {
            return buildPipelineBufferM2(false);
        }
        if (TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER.equals(spec.getTopologyKind())) {
            return buildPipelineBufferM2(false);
        }
        if (TOPOLOGY_BOUNDARY_SPLIT_PIPELINE_BUFFER.equals(spec.getTopologyKind())) {
            return buildPipelineBufferM2(true);
        }
        if (TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER.equals(spec.getTopologyKind())) {
            return buildPipelineBufferM2(true);
        }

        List<Template> list = new ArrayList<Template>();
        for (RandomChannelSpec.ActorSpec actor : spec.getM2Spec()) {
            list.add(buildSimpleActor(actor));
        }
        return list;
    }

    @Override
    public String getNtaPath() {
        return new File(caseDir, "generated-source.xml").getPath();
    }

    @Override
    public Declaration getGlobalDeclaration() {
        Declaration declaration = new Declaration();
        LinkedHashSet<String> channels = collectDeclaredChannels();
        if (!channels.isEmpty()) {
            declaration.put(join(channels), "chan");
        }
        return declaration;
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
        LinkedHashSet<String> sigma = new LinkedHashSet<String>();
        for (String channel : spec.getChannelSpec().getSendChannels()) {
            sigma.add(toTargetAction(channel));
        }
        return sigma;
    }

    @Override
    public PortPreprocessConfig getPortPreprocessConfig() {
        List<String> splitChannels = spec.getChannelSpec().getSplitChannels();
        if (splitChannels == null || splitChannels.isEmpty()) {
            return PortPreprocessConfig.empty();
        }
        return PortPreprocessConfig.bidirectionalDomainSplit(true,
                splitChannels.toArray(new String[splitChannels.size()]));
    }

    @Override
    public ResetPolicyType getResetPolicyType() {
        return ResetPolicyType.STATIC_SIGMA;
    }

    private Template buildBufferMonitor() {
        RandomChannelSpec.MonitorSpec monitor = spec.getM1Spec();
        String name = monitor.getName();
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");

        TemplateBuilder builder = new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocalDeclaration("x", "clock")
                .addLocalDeclaration("count=0,len=" + monitor.getLen(), "int");

        String writeGuard = monitor.isSafeWrite() ? "count < len" : "count <= len";
        for (String channel : writeChannels()) {
            builder.addTransition(new UppaalTransitionBuilder(q0, q0)
                    .addSync(channel + "?")
                    .addGuard(writeGuard)
                    .addAssignment("count = count + 1,x = 0")
                    .getUppaalTransition());
        }
        String readGuard = monitor.isSafeRead() ? "count > 0" : "count >= 0";
        for (String channel : readChannels()) {
            builder.addTransition(new UppaalTransitionBuilder(q0, q0)
                    .addSync(channel + "?")
                    .addGuard(readGuard)
                    .addAssignment("count = count - 1,x = 0")
                    .getUppaalTransition());
        }
        return builder.createTemplate();
    }

    private Template buildMutexMonitor() {
        RandomChannelSpec.MonitorSpec monitor = spec.getM1Spec();
        String name = monitor.getName();
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        TemplateBuilder builder = new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocalDeclaration("x", "clock")
                .addLocalDeclaration("hold=0", "int");
        String enterGuard = monitor.isSafeMutex() ? "hold == 0" : "hold <= 1";
        String enterAssign = monitor.isSafeMutex() ? "hold = 1,x = 0" : "hold = hold + 1,x = 0";
        String exitGuard = monitor.isSafeMutex() ? "hold == 1" : "hold > 0";
        String exitAssign = "hold = hold - 1,x = 0";
        for (int i = 1; i <= monitor.getClientCount(); i++) {
            builder.addTransition(new UppaalTransitionBuilder(q0, q0)
                    .addSync("enter" + i + "?")
                    .addGuard(enterGuard)
                    .addAssignment(enterAssign)
                    .getUppaalTransition());
            builder.addTransition(new UppaalTransitionBuilder(q0, q0)
                    .addSync("exit" + i + "?")
                    .addGuard(exitGuard)
                    .addAssignment(exitAssign)
                    .getUppaalTransition());
        }
        return builder.createTemplate();
    }

    private List<Template> buildSchedBufferM2() {
        List<Template> templates = new ArrayList<Template>();
        for (int i = 1; i <= spec.getWriterCount(); i++) {
            int phaseDepth = effectiveComponentPhaseDepth(i);
            templates.add(buildScheduledTask("WriterTask" + i, externalChannel("write", i),
                    grantChannel("w", i), doneChannel("w", i),
                    releaseLow("w", i), releaseHigh("w", i), fireLow("w", i), fireHigh("w", i), phaseDepth));
        }
        for (int i = 1; i <= spec.getReaderCount(); i++) {
            int phaseDepth = effectiveComponentPhaseDepth(i);
            templates.add(buildScheduledTask("ReaderTask" + i, externalChannel("read", i),
                    grantChannel("r", i), doneChannel("r", i),
                    releaseLow("r", i + spec.getWriterCount()),
                    releaseHigh("r", i + spec.getWriterCount()),
                    fireLow("r", i + spec.getWriterCount()),
                    fireHigh("r", i + spec.getWriterCount()), phaseDepth));
        }
        templates.add(buildDispatcher("Dispatcher", buildBoundaryScheduleSteps()));
        return templates;
    }

    private List<Template> buildPipelineBufferM2(boolean splitPipeline) {
        List<Template> templates = new ArrayList<Template>();
        String splitChannel = normalizeSplitChannel();
        for (int i = 1; i <= spec.getWriterCount(); i++) {
            String external = externalChannel("write", i);
            String handoff = useSplitHandoff(splitPipeline, splitChannel, external, "write", i)
                    ? external
                    : handoffChannel("w", i);
            int phaseDepth = effectiveComponentPhaseDepth(i);
            templates.add(buildPipelineController("WriterCtrl" + i,
                    grantChannel("w", i), handoff,
                    releaseLow("w", i), releaseHigh("w", i), settleLow("w", i), settleHigh("w", i), phaseDepth));
            templates.add(buildPipelineCommitter("WriterCommit" + i,
                    handoff, external, doneChannel("w", i),
                    fireLow("w", i), fireHigh("w", i), settleLow("w", i), settleHigh("w", i), phaseDepth));
        }
        for (int i = 1; i <= spec.getReaderCount(); i++) {
            int offset = i + spec.getWriterCount();
            String external = externalChannel("read", i);
            String handoff = useSplitHandoff(splitPipeline, splitChannel, external, "read", i)
                    ? external
                    : handoffChannel("r", i);
            int phaseDepth = effectiveComponentPhaseDepth(i);
            templates.add(buildPipelineController("ReaderCtrl" + i,
                    grantChannel("r", i), handoff,
                    releaseLow("r", offset), releaseHigh("r", offset), settleLow("r", offset), settleHigh("r", offset),
                    phaseDepth));
            templates.add(buildPipelineCommitter("ReaderCommit" + i,
                    handoff, external, doneChannel("r", i),
                    fireLow("r", offset), fireHigh("r", offset), settleLow("r", offset), settleHigh("r", offset),
                    phaseDepth));
        }
        templates.add(buildDispatcher("Dispatcher", buildBoundaryScheduleSteps()));
        return templates;
    }

    private Template buildScheduledTask(String name, String externalChannel, String grantChannel,
                                        String doneChannel, int releaseLow, int releaseHigh,
                                        int fireLow, int fireHigh, int phaseDepth) {
        UppaalLocation idle = UppaalModelUtil.buildUppaalLocation(name, "idle");
        UppaalLocation release = UppaalModelUtil.buildUppaalLocation(name, "release");
        UppaalLocation ready = UppaalModelUtil.buildUppaalLocation(name, "ready");
        UppaalLocation granted = UppaalModelUtil.buildUppaalLocation(name, "granted");
        UppaalLocation emit = UppaalModelUtil.buildUppaalLocation(name, "emit");

        TemplateBuilder builder = new TemplateBuilder()
                .setName(name)
                .addInitLocation(idle)
                .addLocations(release, ready, granted, emit)
                .addLocalDeclaration("x", "clock");
        List<UppaalLocation> prepStates = createPhaseLocations(name, "prep", phaseDepth);
        List<UppaalLocation> coolStates = createPhaseLocations(name, "cool", phaseDepth);
        addLocations(builder, prepStates);
        addLocations(builder, coolStates);

        builder.addTransition(new UppaalTransitionBuilder(idle, release)
                .addGuard(buildIntervalGuard(releaseLow, releaseHigh))
                .addAssignment("x = 0")
                .getUppaalTransition());
        appendDelayChain(builder, release, prepStates, ready, phaseBaseLow(name), phaseBaseHigh(name));
        builder.addTransition(new UppaalTransitionBuilder(ready, granted)
                .addSync(grantChannel + "?")
                .addAssignment("x = 0")
                .getUppaalTransition());
        builder.addTransition(new UppaalTransitionBuilder(granted, emit)
                .addSync(externalChannel + "!")
                .addGuard(buildIntervalGuard(fireLow, fireHigh))
                .addAssignment("x = 0")
                .getUppaalTransition());
        if (coolStates.isEmpty()) {
            builder.addTransition(new UppaalTransitionBuilder(emit, idle)
                    .addSync(doneChannel + "!")
                    .addAssignment("x = 0")
                    .getUppaalTransition());
        } else {
            builder.addTransition(new UppaalTransitionBuilder(emit, coolStates.get(0))
                    .addSync(doneChannel + "!")
                    .addAssignment("x = 0")
                    .getUppaalTransition());
            appendDelayChain(builder, coolStates.get(0), coolStates.subList(1, coolStates.size()),
                    idle, phaseBaseLow(name) + 1, phaseBaseHigh(name) + 1);
        }
        return builder.createTemplate();
    }

    private Template buildPipelineController(String name, String grantChannel, String handoffChannel,
                                             int releaseLow, int releaseHigh, int settleLow, int settleHigh,
                                             int phaseDepth) {
        UppaalLocation idle = UppaalModelUtil.buildUppaalLocation(name, "idle");
        UppaalLocation release = UppaalModelUtil.buildUppaalLocation(name, "release");
        UppaalLocation ready = UppaalModelUtil.buildUppaalLocation(name, "ready");
        UppaalLocation granted = UppaalModelUtil.buildUppaalLocation(name, "granted");
        UppaalLocation issued = UppaalModelUtil.buildUppaalLocation(name, "issued");

        TemplateBuilder builder = new TemplateBuilder()
                .setName(name)
                .addInitLocation(idle)
                .addLocations(release, ready, granted, issued)
                .addLocalDeclaration("x", "clock");
        List<UppaalLocation> prepStates = createPhaseLocations(name, "prep", phaseDepth);
        List<UppaalLocation> recoverStates = createPhaseLocations(name, "recover", phaseDepth);
        addLocations(builder, prepStates);
        addLocations(builder, recoverStates);

        builder.addTransition(new UppaalTransitionBuilder(idle, release)
                .addGuard(buildIntervalGuard(releaseLow, releaseHigh))
                .addAssignment("x = 0")
                .getUppaalTransition());
        appendDelayChain(builder, release, prepStates, ready, settleLow, settleHigh);
        builder.addTransition(new UppaalTransitionBuilder(ready, granted)
                .addSync(grantChannel + "?")
                .addAssignment("x = 0")
                .getUppaalTransition());
        builder.addTransition(new UppaalTransitionBuilder(granted, issued)
                .addSync(handoffChannel + "!")
                .addGuard(buildIntervalGuard(settleLow, settleHigh))
                .addAssignment("x = 0")
                .getUppaalTransition());
        if (recoverStates.isEmpty()) {
            builder.addTransition(new UppaalTransitionBuilder(issued, idle)
                    .addGuard(buildIntervalGuard(settleLow + 1, settleHigh + 1))
                    .addAssignment("x = 0")
                    .getUppaalTransition());
        } else {
            builder.addTransition(new UppaalTransitionBuilder(issued, recoverStates.get(0))
                    .addGuard(buildIntervalGuard(settleLow + 1, settleHigh + 1))
                    .addAssignment("x = 0")
                    .getUppaalTransition());
            appendDelayChain(builder, recoverStates.get(0), recoverStates.subList(1, recoverStates.size()),
                    idle, settleLow + 1, settleHigh + 1);
        }
        return builder.createTemplate();
    }

    private Template buildPipelineCommitter(String name, String handoffChannel, String externalChannel,
                                            String doneChannel, int fireLow, int fireHigh,
                                            int settleLow, int settleHigh, int phaseDepth) {
        UppaalLocation wait = UppaalModelUtil.buildUppaalLocation(name, "wait");
        UppaalLocation loaded = UppaalModelUtil.buildUppaalLocation(name, "loaded");
        UppaalLocation ready = UppaalModelUtil.buildUppaalLocation(name, "ready");
        UppaalLocation fired = UppaalModelUtil.buildUppaalLocation(name, "fired");

        TemplateBuilder builder = new TemplateBuilder()
                .setName(name)
                .addInitLocation(wait)
                .addLocations(loaded, ready, fired)
                .addLocalDeclaration("x", "clock");
        List<UppaalLocation> stageStates = createPhaseLocations(name, "stage", phaseDepth);
        List<UppaalLocation> settleStates = createPhaseLocations(name, "settle", phaseDepth);
        addLocations(builder, stageStates);
        addLocations(builder, settleStates);

        builder.addTransition(new UppaalTransitionBuilder(wait, loaded)
                .addSync(handoffChannel + "?")
                .addAssignment("x = 0")
                .getUppaalTransition());
        appendDelayChain(builder, loaded, stageStates, ready, settleLow, settleHigh);
        builder.addTransition(new UppaalTransitionBuilder(ready, fired)
                .addSync(externalChannel + "!")
                .addGuard(buildIntervalGuard(fireLow, fireHigh))
                .addAssignment("x = 0")
                .getUppaalTransition());
        if (settleStates.isEmpty()) {
            builder.addTransition(new UppaalTransitionBuilder(fired, wait)
                    .addSync(doneChannel + "!")
                    .addAssignment("x = 0")
                    .getUppaalTransition());
        } else {
            builder.addTransition(new UppaalTransitionBuilder(fired, settleStates.get(0))
                    .addSync(doneChannel + "!")
                    .addAssignment("x = 0")
                    .getUppaalTransition());
            appendDelayChain(builder, settleStates.get(0), settleStates.subList(1, settleStates.size()),
                    wait, settleLow + 1, settleHigh + 1);
        }
        return builder.createTemplate();
    }

    private Template buildDispatcher(String name, List<ScheduleStep> steps) {
        UppaalLocation init = UppaalModelUtil.buildUppaalLocation(name, "s0");
        TemplateBuilder builder = new TemplateBuilder()
                .setName(name)
                .addInitLocation(init)
                .addLocalDeclaration("d", "clock");

        List<UppaalLocation> startStates = new ArrayList<UppaalLocation>();
        startStates.add(init);
        for (int i = 1; i < steps.size(); i++) {
            UppaalLocation start = UppaalModelUtil.buildUppaalLocation(name, "s" + i);
            startStates.add(start);
            builder.addLocation(start);
        }
        List<UppaalLocation> waitStates = new ArrayList<UppaalLocation>();
        for (int i = 0; i < steps.size(); i++) {
            UppaalLocation wait = UppaalModelUtil.buildUppaalLocation(name, "w" + i);
            waitStates.add(wait);
            builder.addLocation(wait);
        }

        for (int i = 0; i < steps.size(); i++) {
            ScheduleStep step = steps.get(i);
            UppaalLocation start = startStates.get(i);
            UppaalLocation wait = waitStates.get(i);
            UppaalLocation next = startStates.get((i + 1) % steps.size());
            builder.addTransition(new UppaalTransitionBuilder(start, wait)
                    .addSync(step.grantChannel + "!")
                    .addGuard("d >= " + step.grantDelay)
                    .addAssignment("d = 0")
                    .getUppaalTransition());
            builder.addTransition(new UppaalTransitionBuilder(wait, next)
                    .addSync(step.doneChannel + "?")
                    .addAssignment("d = 0")
                    .getUppaalTransition());
        }
        return builder.createTemplate();
    }

    private List<ScheduleStep> buildBoundaryScheduleSteps() {
        if (isMidTopology()) {
            return buildModeScheduleSteps();
        }
        List<ScheduleStep> steps = new ArrayList<ScheduleStep>();
        int writerCursor = 1;
        int readerCursor = 1;
        int warmup = Math.max(1, spec.getM1Spec().getLen());
        for (int i = 0; i < warmup; i++) {
            steps.add(new ScheduleStep(grantChannel("w", writerCursor), doneChannel("w", writerCursor),
                    grantDelay(i + 1), "LEGACY"));
            writerCursor = rotate(writerCursor, spec.getWriterCount());
        }
        int rounds = Math.max(2, spec.getDispatcherDepth()) * Math.max(spec.getWriterCount(), spec.getReaderCount());
        for (int i = 0; i < rounds; i++) {
            steps.add(new ScheduleStep(grantChannel("r", readerCursor), doneChannel("r", readerCursor),
                    grantDelay(i + 3), "LEGACY"));
            readerCursor = rotate(readerCursor, spec.getReaderCount());
            steps.add(new ScheduleStep(grantChannel("w", writerCursor), doneChannel("w", writerCursor),
                    grantDelay(i + 4), "LEGACY"));
            writerCursor = rotate(writerCursor, spec.getWriterCount());
        }
        return steps;
    }

    private List<ScheduleStep> buildModeScheduleSteps() {
        List<ScheduleStep> steps = new ArrayList<ScheduleStep>();
        int writerCursor = 1;
        int readerCursor = 1;
        int burst = Math.max(1, spec.getBurstLength());
        int cycles = Math.max(2, spec.getDispatcherDepth());

        if (isOneModePattern()) {
            for (int cycle = 0; cycle < cycles; cycle++) {
                StepCursor oneCursor = appendOneModeSteps(steps, writerCursor, readerCursor, burst);
                writerCursor = oneCursor.writerCursor;
                readerCursor = oneCursor.readerCursor;
            }
            return steps;
        }

        for (int cycle = 0; cycle < cycles; cycle++) {
            StepCursor fillCursor = appendModeSteps(steps, MODE_FILL, writerCursor, readerCursor, burst);
            writerCursor = fillCursor.writerCursor;
            readerCursor = fillCursor.readerCursor;
            if (isThreeModePattern()) {
                StepCursor mixCursor = appendModeSteps(steps, MODE_MIX, writerCursor, readerCursor, burst);
                writerCursor = mixCursor.writerCursor;
                readerCursor = mixCursor.readerCursor;
            }
            StepCursor drainCursor = appendModeSteps(steps, MODE_DRAIN, writerCursor, readerCursor, burst);
            writerCursor = drainCursor.writerCursor;
            readerCursor = drainCursor.readerCursor;
            if (isShowcaseQ3R2()) {
                StepCursor tailMixCursor = appendModeSteps(steps, MODE_MIX, writerCursor, readerCursor, 1);
                writerCursor = tailMixCursor.writerCursor;
                readerCursor = tailMixCursor.readerCursor;
            }
        }
        return steps;
    }

    private StepCursor appendOneModeSteps(List<ScheduleStep> steps, int writerCursor, int readerCursor, int burst) {
        int rounds = Math.max(1, burst);
        for (int i = 0; i < rounds; i++) {
            steps.add(new ScheduleStep(grantChannel("w", writerCursor), doneChannel("w", writerCursor),
                    grantDelay(steps.size() + 1), MODE_MIX));
            writerCursor = rotate(writerCursor, spec.getWriterCount());
            steps.add(new ScheduleStep(grantChannel("r", readerCursor), doneChannel("r", readerCursor),
                    grantDelay(steps.size() + 1), MODE_MIX));
            readerCursor = rotate(readerCursor, spec.getReaderCount());
        }
        return new StepCursor(writerCursor, readerCursor);
    }

    private StepCursor appendModeSteps(List<ScheduleStep> steps, String mode,
                                       int writerCursor, int readerCursor, int burst) {
        if (MODE_FILL.equals(mode)) {
            for (int i = 0; i < burst; i++) {
                steps.add(new ScheduleStep(grantChannel("w", writerCursor), doneChannel("w", writerCursor),
                        grantDelay(steps.size() + 1), mode));
                writerCursor = rotate(writerCursor, spec.getWriterCount());
            }
            return new StepCursor(writerCursor, readerCursor);
        }
        if (MODE_DRAIN.equals(mode)) {
            for (int i = 0; i < burst; i++) {
                steps.add(new ScheduleStep(grantChannel("r", readerCursor), doneChannel("r", readerCursor),
                        grantDelay(steps.size() + 1), mode));
                readerCursor = rotate(readerCursor, spec.getReaderCount());
            }
            return new StepCursor(writerCursor, readerCursor);
        }
        int mixRounds = Math.max(1, burst);
        for (int i = 0; i < mixRounds; i++) {
            steps.add(new ScheduleStep(grantChannel("r", readerCursor), doneChannel("r", readerCursor),
                    grantDelay(steps.size() + 1), mode));
            readerCursor = rotate(readerCursor, spec.getReaderCount());
            steps.add(new ScheduleStep(grantChannel("w", writerCursor), doneChannel("w", writerCursor),
                    grantDelay(steps.size() + 1), mode));
            writerCursor = rotate(writerCursor, spec.getWriterCount());
        }
        return new StepCursor(writerCursor, readerCursor);
    }

    private Template buildSimpleActor(RandomChannelSpec.ActorSpec actor) {
        String kind = actor.getKind();
        if (ACTOR_WRITER.equals(kind) || ACTOR_READER.equals(kind) || ACTOR_INTERNAL_SENDER.equals(kind)) {
            return buildLoopActor(actor.getName(), actor.getChannel(), "!", actor.getLow(), actor.getHigh());
        }
        if (ACTOR_INTERNAL_RECEIVER.equals(kind)) {
            return buildLoopActor(actor.getName(), actor.getChannel(), "?", actor.getLow(), actor.getHigh());
        }
        if (ACTOR_CLIENT.equals(kind)) {
            return buildClientActor(actor);
        }
        throw new IllegalStateException("Unsupported actor kind: " + kind);
    }

    private Template buildLoopActor(String name, String channel, String suffix, int low, int high) {
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        UppaalTransition t = new UppaalTransitionBuilder(q0, q0)
                .addSync(channel + suffix)
                .addGuard(buildIntervalGuard(low, high))
                .addAssignment("x = 0")
                .getUppaalTransition();
        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocalDeclaration("x", "clock")
                .addTransition(t)
                .createTemplate();
    }

    private Template buildClientActor(RandomChannelSpec.ActorSpec actor) {
        String name = actor.getName();
        UppaalLocation idle = UppaalModelUtil.buildUppaalLocation(name, "idle");
        UppaalLocation in = UppaalModelUtil.buildUppaalLocation(name, "in");
        UppaalTransition enter = new UppaalTransitionBuilder(idle, in)
                .addSync(actor.getEnterChannel() + "!")
                .addGuard(buildIntervalGuard(actor.getLow(), actor.getHigh()))
                .addAssignment("x = 0")
                .getUppaalTransition();
        UppaalTransition exit = new UppaalTransitionBuilder(in, idle)
                .addSync(actor.getExitChannel() + "!")
                .addGuard(buildIntervalGuard(actor.getSecondLow(), actor.getSecondHigh()))
                .addAssignment("x = 0")
                .getUppaalTransition();
        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(idle)
                .addLocation(in)
                .addLocalDeclaration("x", "clock")
                .addTransitions(enter, exit)
                .createTemplate();
    }

    private LinkedHashSet<String> collectDeclaredChannels() {
        LinkedHashSet<String> channels = new LinkedHashSet<String>();
        if (spec.getChannelSpec() != null && spec.getChannelSpec().getSendChannels() != null) {
            channels.addAll(spec.getChannelSpec().getSendChannels());
        }

        String topology = spec.getTopologyKind();
        if (TOPOLOGY_BOUNDARY_SCHED_BUFFER.equals(topology)
                || TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER.equals(topology)) {
            addDispatcherChannels(channels, false);
            return channels;
        }
        if (TOPOLOGY_BOUNDARY_PIPELINE_BUFFER.equals(topology)
                || TOPOLOGY_BOUNDARY_SPLIT_PIPELINE_BUFFER.equals(topology)
                || TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER.equals(topology)
                || TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER.equals(topology)) {
            addDispatcherChannels(channels, true);
            return channels;
        }

        for (RandomChannelSpec.ActorSpec actor : spec.getM2Spec()) {
            addNonEmpty(channels, actor.getChannel());
            addNonEmpty(channels, actor.getEnterChannel());
            addNonEmpty(channels, actor.getExitChannel());
        }
        return channels;
    }

    private void addDispatcherChannels(Set<String> channels, boolean includeHandoff) {
        String splitChannel = normalizeSplitChannel();
        for (int i = 1; i <= spec.getWriterCount(); i++) {
            channels.add(grantChannel("w", i));
            channels.add(doneChannel("w", i));
            if (includeHandoff) {
                channels.add(splitHandoffChannel(externalChannel("write", i), splitChannel, "w", i));
            }
        }
        for (int i = 1; i <= spec.getReaderCount(); i++) {
            channels.add(grantChannel("r", i));
            channels.add(doneChannel("r", i));
            if (includeHandoff) {
                channels.add(splitHandoffChannel(externalChannel("read", i), splitChannel, "r", i));
            }
        }
    }

    private String normalizeSplitChannel() {
        String split = spec.getSplitChannel();
        return split == null ? "" : split.trim();
    }

    private String splitHandoffChannel(String externalChannel, String splitChannel, String role, int index) {
        if (externalChannel.equals(splitChannel) && isFirstIndexForChannel("w".equals(role) ? "write" : "read", externalChannel, index)) {
            return externalChannel;
        }
        return handoffChannel(role, index);
    }

    private static void addLocations(TemplateBuilder builder, List<UppaalLocation> locations) {
        for (UppaalLocation location : locations) {
            builder.addLocation(location);
        }
    }

    private static List<UppaalLocation> createPhaseLocations(String templateName, String prefix, int phaseDepth) {
        List<UppaalLocation> locations = new ArrayList<UppaalLocation>();
        for (int i = 1; i <= Math.max(0, phaseDepth); i++) {
            locations.add(UppaalModelUtil.buildUppaalLocation(templateName, prefix + i));
        }
        return locations;
    }

    private static void appendDelayChain(TemplateBuilder builder, UppaalLocation source,
                                         List<UppaalLocation> middleStates, UppaalLocation target,
                                         int low, int high) {
        UppaalLocation current = source;
        for (UppaalLocation middle : middleStates) {
            builder.addTransition(new UppaalTransitionBuilder(current, middle)
                    .addGuard(buildIntervalGuard(low, high))
                    .addAssignment("x = 0")
                    .getUppaalTransition());
            current = middle;
        }
        builder.addTransition(new UppaalTransitionBuilder(current, target)
                .addGuard(buildIntervalGuard(low, high))
                .addAssignment("x = 0")
                .getUppaalTransition());
    }

    private static int rotate(int current, int bound) {
        if (bound <= 1) {
            return 1;
        }
        return (current % bound) + 1;
    }

    private static int phaseBaseLow(String name) {
        return 1 + (Math.abs(name.hashCode()) % 2);
    }

    private static int phaseBaseHigh(String name) {
        return phaseBaseLow(name) + 1;
    }

    private List<String> writeChannels() {
        List<String> channels = new ArrayList<String>();
        if (spec.getChannelSpec().getSendChannels() == null) {
            return channels;
        }
        for (String channel : spec.getChannelSpec().getSendChannels()) {
            if (channel != null && channel.startsWith("write")) {
                channels.add(channel);
            }
        }
        return channels;
    }

    private List<String> readChannels() {
        List<String> channels = new ArrayList<String>();
        if (spec.getChannelSpec().getSendChannels() == null) {
            return channels;
        }
        for (String channel : spec.getChannelSpec().getSendChannels()) {
            if (channel != null && channel.startsWith("read")) {
                channels.add(channel);
            }
        }
        return channels;
    }

    private String externalChannel(String role, int index) {
        List<String> channels = role.startsWith("write") ? writeChannels() : readChannels();
        if (channels.isEmpty()) {
            return role;
        }
        return channels.get((index - 1) % channels.size());
    }

    private boolean useSplitHandoff(boolean splitPipeline, String splitChannel, String externalChannel,
                                    String role, int index) {
        return splitPipeline
                && externalChannel.equals(splitChannel)
                && isFirstIndexForChannel(role, externalChannel, index);
    }

    private boolean isFirstIndexForChannel(String role, String externalChannel, int index) {
        int bound = role.startsWith("write") ? spec.getWriterCount() : spec.getReaderCount();
        for (int i = 1; i <= bound; i++) {
            if (externalChannel(role, i).equals(externalChannel)) {
                return i == index;
            }
        }
        return false;
    }

    private String toTargetAction(String channel) {
        if (spec.getChannelSpec().getSplitChannels() != null
                && spec.getChannelSpec().getSplitChannels().contains(channel)) {
            return channel + "_m2_to_m1!";
        }
        return channel + "!";
    }

    private int releaseLow(String role, int index) {
        if (isMidTopology()) {
            int base = isTightBoundaryMode() ? 1 : 1 + (index % 2);
            return adjustRoleTiming(role, base, true);
        }
        return 1 + (index % 2);
    }

    private int releaseHigh(String role, int index) {
        int low = releaseLow(role, index);
        if (isMidTopology() && isTightBoundaryMode()) {
            return low;
        }
        return low + 1;
    }

    private int fireLow(String role, int index) {
        if (isMidTopology()) {
            int base = isTightBoundaryMode() ? 1 : 1 + (index % 2);
            return adjustRoleTiming(role, base, false);
        }
        return 1 + (index % 2);
    }

    private int fireHigh(String role, int index) {
        int low = fireLow(role, index);
        if (isMidTopology() && isTightBoundaryMode()) {
            return low;
        }
        return low + 1;
    }

    private int settleLow(String role, int index) {
        if (isMidTopology()) {
            int base = isTightBoundaryMode() ? 1 : 1 + (index % 2);
            if (isMidSplitTopology()) {
                return role.startsWith("r") ? Math.max(1, base - 1) : base + 1;
            }
            return base;
        }
        return 1 + (index % 2);
    }

    private int settleHigh(String role, int index) {
        int low = settleLow(role, index);
        if (isMidTopology() && isTightBoundaryMode()) {
            return low;
        }
        return low + 1;
    }

    private int grantDelay(int index) {
        if (isMidTopology()) {
            return isTightBoundaryMode() ? 1 + (index % 2) : 2 + (index % 2);
        }
        return 2 + (index % 2);
    }

    private boolean isMidTopology() {
        String topology = spec.getTopologyKind();
        return TOPOLOGY_MID_BOUNDARY_SCHED_BUFFER.equals(topology)
                || TOPOLOGY_MID_BOUNDARY_PIPELINE_BUFFER.equals(topology)
                || TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER.equals(topology);
    }

    private boolean isTightBoundaryMode() {
        return "TIGHT".equalsIgnoreCase(spec.getNearBoundaryMode());
    }

    private boolean isOneModePattern() {
        return MODE_ONE.equalsIgnoreCase(spec.getModePattern());
    }

    private boolean isThreeModePattern() {
        return MODE_THREE.equalsIgnoreCase(spec.getModePattern());
    }

    private boolean isMidSplitTopology() {
        return TOPOLOGY_MID_BOUNDARY_SPLIT_PIPELINE_BUFFER.equals(spec.getTopologyKind());
    }

    private boolean isStressProfile() {
        return PROFILE_S6_STRESS.equalsIgnoreCase(spec.getCaseProfile());
    }

    private boolean isShowcaseQ3R2() {
        return "Q3_R2".equalsIgnoreCase(spec.getShowcaseTarget());
    }

    private int effectiveComponentPhaseDepth(int index) {
        int base = spec.getPhaseDepth();
        if (!isStressProfile()) {
            return base;
        }
        return index >= 3 ? Math.max(0, base - 1) : base;
    }

    private int adjustRoleTiming(String role, int base, boolean releaseStage) {
        if (!isMidSplitTopology()) {
            return base;
        }
        if (role.startsWith("r")) {
            return Math.max(1, base - 1);
        }
        return releaseStage ? base + 1 : base + 2;
    }

    private static String grantChannel(String role, int index) {
        return "grant_" + role + index;
    }

    private static String doneChannel(String role, int index) {
        return "done_" + role + index;
    }

    private static String handoffChannel(String role, int index) {
        return "handoff_" + role + index;
    }

    private static String buildIntervalGuard(int low, int high) {
        return "x >= " + low + " && x <= " + high;
    }

    private static String join(Set<String> values) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String value : values) {
            if (index++ > 0) {
                sb.append(",");
            }
            sb.append(value);
        }
        return sb.toString();
    }

    private static void addNonEmpty(Set<String> channels, String channel) {
        if (channel == null) {
            return;
        }
        String trimmed = channel.trim();
        if (!trimmed.isEmpty()) {
            channels.add(trimmed);
        }
    }

    private static final class ScheduleStep {
        private final String grantChannel;
        private final String doneChannel;
        private final int grantDelay;
        private final String mode;

        private ScheduleStep(String grantChannel, String doneChannel, int grantDelay, String mode) {
            this.grantChannel = grantChannel;
            this.doneChannel = doneChannel;
            this.grantDelay = grantDelay;
            this.mode = mode;
        }
    }

    private static final class StepCursor {
        private final int writerCursor;
        private final int readerCursor;

        private StepCursor(int writerCursor, int readerCursor) {
            this.writerCursor = writerCursor;
            this.readerCursor = readerCursor;
        }
    }
}
