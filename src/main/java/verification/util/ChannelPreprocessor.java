package verification.util;

import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.uppaal.model.UppaalTransition;
import verification.uppaal.model.label.AssignmentLabel;
import verification.uppaal.model.label.GuardLabel;
import verification.uppaal.model.label.SelectLabel;
import verification.uppaal.model.label.SynchronizedLabel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ChannelPreprocessor {
    private ChannelPreprocessor() {
    }

    public static ChannelAliasRegistry preprocessPortMode(List<Template> m1,
                                                          List<Template> m2,
                                                          Map<String, String> m1RenameMap,
                                                          Map<String, String> m2RenameMap,
                                                          Set<String> targetSigma) {
        return preprocessPortMode(
                null, m1, m2, m1RenameMap, m2RenameMap, targetSigma, PortPreprocessConfig.empty());
    }

    public static ChannelAliasRegistry preprocessPortMode(Declaration globalDeclaration,
                                                          List<Template> m1,
                                                          List<Template> m2,
                                                          Map<String, String> m1RenameMap,
                                                          Map<String, String> m2RenameMap,
                                                          Set<String> targetSigma,
                                                          PrimeSplitConfig primeCfg) {
        return preprocessPortMode(
                globalDeclaration,
                m1,
                m2,
                m1RenameMap,
                m2RenameMap,
                targetSigma,
                primeCfg == null || primeCfg.isEmpty()
                        ? PortPreprocessConfig.empty()
                        : PortPreprocessConfig.primeSplit(primeCfg.getChannels()));
    }

    public static ChannelAliasRegistry preprocessPortMode(Declaration globalDeclaration,
                                                          List<Template> m1,
                                                          List<Template> m2,
                                                          Map<String, String> m1RenameMap,
                                                          Map<String, String> m2RenameMap,
                                                          Set<String> targetSigma,
                                                          PortPreprocessConfig preprocessConfig) {
        PortPreprocessConfig config = preprocessConfig == null
                ? PortPreprocessConfig.empty()
                : preprocessConfig;

        validateTargetSigma(targetSigma);
        renameByMap(m1, m1RenameMap);
        renameByMap(m2, m2RenameMap);

        Set<String> conflictExemptChannels = new HashSet<String>();
        ChannelAliasRegistry aliasRegistry = new ChannelAliasRegistry();
        if (!config.isEmpty()) {
            if (config.getMode() == PortSplitMode.PRIME_SPLIT) {
                applyPrimeSplits(globalDeclaration, m1, m2, targetSigma, toPrimeSplitConfig(config.getChannels()));
            } else if (config.getMode() == PortSplitMode.BIDIRECTIONAL_DOMAIN_SPLIT) {
                conflictExemptChannels.addAll(applyBidirectionalDomainSplits(
                        globalDeclaration, m1, m2, config.getChannels(), aliasRegistry));
            } else {
                throw new IllegalStateException("Unsupported port split mode: " + config.getMode());
            }
        }

        if (config.isLogicalAliasView() && !aliasRegistry.isEmpty()) {
            printAliasRegistry(aliasRegistry);
        }

        validateInterfaceConsistency(m1, m2, targetSigma);
        detectInternalExternalChannelConflicts(m1, m2, conflictExemptChannels);
        return aliasRegistry;
    }

    private static PrimeSplitConfig toPrimeSplitConfig(Set<String> channels) {
        if (channels == null || channels.isEmpty()) {
            return PrimeSplitConfig.empty();
        }
        return PrimeSplitConfig.of(channels.toArray(new String[channels.size()]));
    }

    private static void validateTargetSigma(Set<String> targetSigma) {
        if (targetSigma == null || targetSigma.isEmpty()) {
            throw new IllegalArgumentException("targetSigma must not be empty in port mode.");
        }
        for (String action : targetSigma) {
            if (!PortActionUtil.isPortAction(action)) {
                throw new IllegalArgumentException("Invalid target sigma in port mode: " + action);
            }
        }
    }

    private static void renameByMap(List<Template> templates, Map<String, String> renameMap) {
        if (templates == null || renameMap == null || renameMap.isEmpty()) {
            return;
        }
        for (Template template : templates) {
            String templateName = template.getName();
            if (template.getUppaalTransitionList() == null) {
                continue;
            }
            for (UppaalTransition transition : template.getUppaalTransitionList()) {
                if (transition.getSynchronizedLabel() == null) {
                    continue;
                }
                String label = PortActionUtil.normalize(transition.getSynchronizedLabel().getText());
                if (!PortActionUtil.isPortAction(label)) {
                    continue;
                }
                String channel = PortActionUtil.channelOf(label);
                String scopedKey = templateName + ":" + channel;
                String target = renameMap.get(scopedKey);
                if (target == null) {
                    target = renameMap.get(channel);
                }
                if (target == null || target.trim().isEmpty()) {
                    continue;
                }
                char suffix = label.charAt(label.length() - 1);
                transition.getSynchronizedLabel().setText(target.trim() + suffix);
            }
        }
    }

    private static Set<String> applyBidirectionalDomainSplits(Declaration globalDeclaration,
                                                               List<Template> m1,
                                                               List<Template> m2,
                                                               Set<String> splitChannels,
                                                               ChannelAliasRegistry aliasRegistry) {
        Set<String> transformedChannels = new HashSet<String>();
        if (splitChannels == null || splitChannels.isEmpty()) {
            return transformedChannels;
        }

        Set<String> declaredChannels = collectDeclaredChannels(globalDeclaration);
        for (String baseChannel : splitChannels) {
            if (baseChannel == null || baseChannel.trim().isEmpty()) {
                continue;
            }
            String channel = baseChannel.trim();
            String m1ToM2 = channel + "_m1_to_m2";
            String m2ToM1 = channel + "_m2_to_m1";
            String m1Int = channel + "_m1_int";
            String m2Int = channel + "_m2_int";

            ensureChannelDeclared(globalDeclaration, declaredChannels, m1ToM2);
            ensureChannelDeclared(globalDeclaration, declaredChannels, m2ToM1);
            ensureChannelDeclared(globalDeclaration, declaredChannels, m1Int);
            ensureChannelDeclared(globalDeclaration, declaredChannels, m2Int);

            int duplicated = 0;
            duplicated += rewriteDomainSplitTransitions(m1, true, channel, m1ToM2, m2ToM1, m1Int, m2Int);
            duplicated += rewriteDomainSplitTransitions(m2, false, channel, m1ToM2, m2ToM1, m1Int, m2Int);

            aliasRegistry.registerActionAlias(m1ToM2 + "!", channel + "!");
            aliasRegistry.registerActionAlias(m1ToM2 + "?", channel + "?");
            aliasRegistry.registerActionAlias(m2ToM1 + "!", channel + "!");
            aliasRegistry.registerActionAlias(m2ToM1 + "?", channel + "?");
            aliasRegistry.registerActionAlias(m1Int + "!", channel + "!");
            aliasRegistry.registerActionAlias(m1Int + "?", channel + "?");
            aliasRegistry.registerActionAlias(m2Int + "!", channel + "!");
            aliasRegistry.registerActionAlias(m2Int + "?", channel + "?");

            transformedChannels.add(channel);
            System.out.println("Domain split applied: " + channel);
            System.out.println("Created channels: [" + m1ToM2 + ", " + m2ToM1 + ", " + m1Int + ", " + m2Int + "]");
            System.out.println("Duplicated transitions: " + duplicated);
        }
        return transformedChannels;
    }

    private static int rewriteDomainSplitTransitions(List<Template> templates,
                                                     boolean inM1,
                                                     String baseChannel,
                                                     String m1ToM2,
                                                     String m2ToM1,
                                                     String m1Int,
                                                     String m2Int) {
        if (templates == null) {
            return 0;
        }
        int duplicated = 0;
        for (Template template : templates) {
            List<UppaalTransition> transitions = template.getUppaalTransitionList();
            if (transitions == null || transitions.isEmpty()) {
                continue;
            }
            List<UppaalTransition> snapshot = new ArrayList<UppaalTransition>(transitions);
            for (UppaalTransition transition : snapshot) {
                if (transition.getSynchronizedLabel() == null) {
                    continue;
                }
                String action = PortActionUtil.normalize(transition.getSynchronizedLabel().getText());
                if (!PortActionUtil.isPortAction(action)) {
                    continue;
                }
                if (!baseChannel.equals(PortActionUtil.channelOf(action))) {
                    continue;
                }

                String externalAction;
                String internalAction;
                if (PortActionUtil.isSend(action)) {
                    externalAction = inM1 ? m1ToM2 + "!" : m2ToM1 + "!";
                    internalAction = inM1 ? m1Int + "!" : m2Int + "!";
                } else {
                    externalAction = inM1 ? m2ToM1 + "?" : m1ToM2 + "?";
                    internalAction = inM1 ? m1Int + "?" : m2Int + "?";
                }

                transition.getSynchronizedLabel().setText(externalAction);
                transitions.add(cloneTransitionWithSync(transition, internalAction));
                duplicated++;
            }
        }
        return duplicated;
    }

    private static void applyPrimeSplits(Declaration globalDeclaration,
                                         List<Template> m1,
                                         List<Template> m2,
                                         Set<String> targetSigma,
                                         PrimeSplitConfig primeCfg) {
        if (primeCfg == null || primeCfg.isEmpty()) {
            return;
        }

        Set<String> declaredChannels = collectDeclaredChannels(globalDeclaration);
        for (String channel : primeCfg.getChannels()) {
            String interfaceAction = resolveUniqueInterfaceAction(channel, targetSigma);
            PrimeSplitStats stats = applyPrimeSplitForChannel(channel, interfaceAction, m1, m2);
            if (stats.senderCount <= 1) {
                continue;
            }
            for (int i = 1; i < stats.senderCount; i++) {
                String internalChannel = PrimeChannelNamer.internalName(channel, i);
                ensureChannelDeclared(globalDeclaration, declaredChannels, internalChannel);
            }
            printPrimeSplitLog(channel, stats.senderCount, stats.renamedSenders, stats.duplicatedReceivers);
        }
    }

    private static PrimeSplitStats applyPrimeSplitForChannel(String channel,
                                                             String interfaceAction,
                                                             List<Template> m1,
                                                             List<Template> m2) {
        List<TransitionRef> senders = new ArrayList<TransitionRef>();
        List<TransitionRef> receivers = new ArrayList<TransitionRef>();
        collectChannelTransitions(senders, receivers, m1, true, channel);
        collectChannelTransitions(senders, receivers, m2, false, channel);

        List<SenderGroup> senderGroups = groupSenders(senders);
        int senderCount = senderGroups.size();
        if (senderCount <= 1) {
            return new PrimeSplitStats(senderCount, 0, 0);
        }

        int primarySenderIndex = pickPrimarySenderIndex(senderGroups, interfaceAction);
        int primeIndex = 1;
        int renamed = 0;
        for (int i = 0; i < senderGroups.size(); i++) {
            if (i == primarySenderIndex) {
                continue;
            }
            SenderGroup group = senderGroups.get(i);
            String renamedChannel = PrimeChannelNamer.internalName(channel, primeIndex);
            for (TransitionRef ref : group.transitions) {
                ref.transition.getSynchronizedLabel().setText(renamedChannel + "!");
                renamed++;
            }
            primeIndex++;
        }

        int duplicated = 0;
        if (!receivers.isEmpty()) {
            for (TransitionRef receiver : receivers) {
                List<UppaalTransition> transitions = receiver.template.getUppaalTransitionList();
                for (int i = 1; i < senderCount; i++) {
                    String renamedChannel = PrimeChannelNamer.internalName(channel, i);
                    transitions.add(cloneTransitionWithSync(receiver.transition, renamedChannel + "?"));
                    duplicated++;
                }
            }
        }
        return new PrimeSplitStats(senderCount, renamed, duplicated);
    }

    private static List<SenderGroup> groupSenders(List<TransitionRef> senders) {
        Map<String, SenderGroup> groups = new HashMap<String, SenderGroup>();
        List<String> order = new ArrayList<String>();
        for (TransitionRef ref : senders) {
            String key = buildSenderGroupKey(ref);
            SenderGroup group = groups.get(key);
            if (group == null) {
                group = new SenderGroup(ref.inM1);
                groups.put(key, group);
                order.add(key);
            }
            group.transitions.add(ref);
        }
        List<SenderGroup> result = new ArrayList<SenderGroup>();
        for (String key : order) {
            result.add(groups.get(key));
        }
        return result;
    }

    private static String buildSenderGroupKey(TransitionRef ref) {
        return ref.template.getName()
                + "|" + ref.transition.getSource().getId()
                + "|" + ref.transition.getTarget().getId();
    }

    private static void collectChannelTransitions(List<TransitionRef> senders,
                                                  List<TransitionRef> receivers,
                                                  List<Template> templates,
                                                  boolean inM1,
                                                  String channel) {
        if (templates == null) {
            return;
        }
        for (Template template : templates) {
            List<UppaalTransition> transitions = template.getUppaalTransitionList();
            if (transitions == null) {
                continue;
            }
            for (UppaalTransition transition : transitions) {
                if (transition.getSynchronizedLabel() == null) {
                    continue;
                }
                String action = PortActionUtil.normalize(transition.getSynchronizedLabel().getText());
                if (!PortActionUtil.isPortAction(action)) {
                    continue;
                }
                if (!channel.equals(PortActionUtil.channelOf(action))) {
                    continue;
                }
                if (PortActionUtil.isSend(action)) {
                    senders.add(new TransitionRef(template, transition, inM1));
                } else if (PortActionUtil.isReceive(action)) {
                    receivers.add(new TransitionRef(template, transition, inM1));
                }
            }
        }
    }

    private static int pickPrimarySenderIndex(List<SenderGroup> senders, String interfaceAction) {
        boolean preferM1Sender = PortActionUtil.isReceive(interfaceAction);
        for (int i = 0; i < senders.size(); i++) {
            if (senders.get(i).inM1 == preferM1Sender) {
                return i;
            }
        }
        return 0;
    }

    private static UppaalTransition cloneTransitionWithSync(UppaalTransition original, String syncText) {
        UppaalTransition cloned = new UppaalTransition(original.getSource(), original.getTarget());
        if (original.getSelectLabel() != null) {
            SelectLabel select = new SelectLabel(original.getSelectLabel().getText());
            cloned.setSelectLabel(select);
        }
        if (original.getGuardLabel() != null) {
            GuardLabel guard = new GuardLabel();
            guard.setText(original.getGuardLabel().getText());
            cloned.setGuardLabel(guard);
        }
        if (syncText != null) {
            SynchronizedLabel sync = new SynchronizedLabel();
            sync.setText(syncText);
            cloned.setSynchronizedLabel(sync);
        }
        if (original.getAssignmentLabel() != null) {
            AssignmentLabel assignment = new AssignmentLabel();
            assignment.setText(original.getAssignmentLabel().getText());
            cloned.setAssignmentLabel(assignment);
        }
        return cloned;
    }

    private static String resolveUniqueInterfaceAction(String channel, Set<String> targetSigma) {
        List<String> matches = new ArrayList<String>();
        for (String action : targetSigma) {
            String normalized = PortActionUtil.normalize(action);
            if (PortActionUtil.isPortAction(normalized)
                    && channel.equals(PortActionUtil.channelOf(normalized))) {
                matches.add(normalized);
            }
        }
        if (matches.isEmpty()) {
            throw new IllegalStateException("Prime split channel '" + channel
                    + "' is not present in targetSigma.");
        }
        if (matches.size() > 1) {
            throw new IllegalStateException("Prime split does not support bidirectional interface channel '"
                    + channel + "' in one round: " + matches);
        }
        return matches.get(0);
    }

    private static Set<String> collectDeclaredChannels(Declaration declaration) {
        Set<String> channels = new LinkedHashSet<String>();
        if (declaration == null || !declaration.isMapInitialized()) {
            return channels;
        }
        for (Map.Entry<String, String> entry : declaration.getMap().entrySet()) {
            String type = entry.getValue();
            if (type == null || !type.contains("chan")) {
                continue;
            }
            for (String raw : entry.getKey().split(",")) {
                String channel = raw.trim();
                if (!channel.isEmpty()) {
                    channels.add(channel);
                }
            }
        }
        return channels;
    }

    private static void ensureChannelDeclared(Declaration declaration,
                                              Set<String> declaredChannels,
                                              String channel) {
        if (declaredChannels.contains(channel)) {
            return;
        }
        if (declaration == null) {
            return;
        }
        declaration.put(channel, "chan");
        declaredChannels.add(channel);
    }

    private static void printPrimeSplitLog(String channel,
                                           int senderCount,
                                           int renamedSenders,
                                           int duplicatedReceivers) {
        List<String> realNames = new ArrayList<String>();
        realNames.add(channel);
        for (int i = 1; i < senderCount; i++) {
            realNames.add(PrimeChannelNamer.internalName(channel, i));
        }
        System.out.println("Prime split applied: " + channel + " -> " + realNames);
        System.out.println("Renamed senders: " + renamedSenders);
        System.out.println("Duplicated receivers: " + duplicatedReceivers);
        for (int i = 1; i < senderCount; i++) {
            String real = PrimeChannelNamer.internalName(channel, i);
            String display = PrimeChannelNamer.displayName(channel, i);
            System.out.println("Display map: " + real + " => " + display);
        }
    }

    private static void printAliasRegistry(ChannelAliasRegistry aliasRegistry) {
        System.out.println("Logical alias view:");
        for (Map.Entry<String, String> entry : aliasRegistry.view().entrySet()) {
            System.out.println("  " + entry.getKey() + " => " + entry.getValue());
        }
    }

    private static void validateInterfaceConsistency(List<Template> m1, List<Template> m2, Set<String> targetSigma) {
        Set<String> m1Actions = collectPortActions(m1);
        Set<String> m2Actions = collectPortActions(m2);
        List<String> errors = new ArrayList<String>();
        for (String envAction : targetSigma) {
            if (!m2Actions.contains(envAction)) {
                errors.add("M2 missing interface action: " + envAction);
            }
            String comp = PortActionUtil.complement(envAction);
            if (!m1Actions.contains(comp)) {
                errors.add("M1 missing complementary action for " + envAction + ": " + comp);
            }
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Port interface consistency check failed: " + errors);
        }
    }

    private static void detectInternalExternalChannelConflicts(List<Template> m1,
                                                               List<Template> m2,
                                                               Set<String> exemptChannels) {
        Map<String, Usage> m1Usage = collectChannelUsage(m1);
        Map<String, Usage> m2Usage = collectChannelUsage(m2);
        Set<String> common = new HashSet<String>(m1Usage.keySet());
        common.retainAll(m2Usage.keySet());

        List<String> conflicts = new ArrayList<String>();
        for (String channel : common) {
            if (exemptChannels != null && exemptChannels.contains(channel)) {
                continue;
            }
            Usage m1u = m1Usage.get(channel);
            Usage m2u = m2Usage.get(channel);
            if (m1u.hasBothDirections() || m2u.hasBothDirections()) {
                conflicts.add(channel);
            }
        }
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Detected internal/external channel conflict(s): " + conflicts
                    + ". Please rename internal channels before learning.");
        }
    }

    private static Set<String> collectPortActions(List<Template> templates) {
        Set<String> actions = new HashSet<String>();
        for (Template template : templates) {
            if (template.getUppaalTransitionList() == null) {
                continue;
            }
            for (UppaalTransition transition : template.getUppaalTransitionList()) {
                if (transition.getSynchronizedLabel() == null) {
                    continue;
                }
                String action = PortActionUtil.normalize(transition.getSynchronizedLabel().getText());
                if (PortActionUtil.isPortAction(action)) {
                    actions.add(action);
                }
            }
        }
        return actions;
    }

    private static Map<String, Usage> collectChannelUsage(List<Template> templates) {
        Map<String, Usage> usageMap = new HashMap<String, Usage>();
        for (Template template : templates) {
            if (template.getUppaalTransitionList() == null) {
                continue;
            }
            for (UppaalTransition transition : template.getUppaalTransitionList()) {
                if (transition.getSynchronizedLabel() == null) {
                    continue;
                }
                String action = PortActionUtil.normalize(transition.getSynchronizedLabel().getText());
                if (!PortActionUtil.isPortAction(action)) {
                    continue;
                }
                String channel = PortActionUtil.channelOf(action);
                Usage usage = usageMap.get(channel);
                if (usage == null) {
                    usage = new Usage();
                    usageMap.put(channel, usage);
                }
                if (PortActionUtil.isSend(action)) {
                    usage.send = true;
                } else if (PortActionUtil.isReceive(action)) {
                    usage.receive = true;
                }
            }
        }
        return usageMap;
    }

    private static class Usage {
        private boolean send;
        private boolean receive;

        private boolean hasBothDirections() {
            return send && receive;
        }
    }

    private static class TransitionRef {
        private final Template template;
        private final UppaalTransition transition;
        private final boolean inM1;

        private TransitionRef(Template template, UppaalTransition transition, boolean inM1) {
            this.template = template;
            this.transition = transition;
            this.inM1 = inM1;
        }
    }

    private static class PrimeSplitStats {
        private final int senderCount;
        private final int renamedSenders;
        private final int duplicatedReceivers;

        private PrimeSplitStats(int senderCount, int renamedSenders, int duplicatedReceivers) {
            this.senderCount = senderCount;
            this.renamedSenders = renamedSenders;
            this.duplicatedReceivers = duplicatedReceivers;
        }
    }

    private static class SenderGroup {
        private final boolean inM1;
        private final List<TransitionRef> transitions = new ArrayList<TransitionRef>();

        private SenderGroup(boolean inM1) {
            this.inM1 = inM1;
        }
    }
}
