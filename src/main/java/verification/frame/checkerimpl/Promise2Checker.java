package verification.frame.checkerimpl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import ta.ota.DOTA;
import ta.ota.DOTAUtil;
import ta.ota.ResetLogicTimeWord;
import verification.Config;
import verification.frame.Cq2Mode;
import verification.frame.Checker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.NTA;
import verification.uppaal.model.Template;
import verification.uppaal.model.UppaalLocation;
import verification.uppaal.model.UppaalTransition;
import verification.uppaal.model.builder.TemplateBuilder;
import verification.uppaal.model.builder.UppaalTransitionBuilder;
import verification.uppaal.verify.Result;
import verification.uppaal.verify.Verifyta;
import verification.util.PortActionUtil;
import verification.util.UppaalModelUtil;
import verification.util.VerificationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class Promise2Checker implements Checker<DOTA> {
    private List<Template> m2;
    private Map<String, Boolean> syncSendMap;
    private Declaration globalDeclaration;
    private Set<String> targetSigma;
    private boolean portActionMode;
    private Cq2Mode cq2Mode;

    public Result isSatisfied(DOTA assumption, Set<String> downSet) {
        assumption = VerificationUtil.removeSink(assumption);
        int beforeLocationCount = assumption.getLocations().size();
        assumption.setSigma(downSet);
        DOTAUtil.completeDOTA(assumption);
        if (assumption.getLocations().size() == beforeLocationCount) {
            return new Result(true, null, null);
        }

        Set<String> baseSigma = portActionMode ? targetSigma : syncSendMap.keySet();
        Set<String> ignoreSigmas = baseSigma.stream().filter(t -> !downSet.contains(t)).collect(Collectors.toSet());

        if (cq2Mode == Cq2Mode.MIRROR_WITH_UNIVERSAL_PARTNER && portActionMode) {
            System.out.println("CQ2 mode: MIRROR_WITH_UNIVERSAL_PARTNER (M2 || U || A^mir)");
            return verifyWithMirrorAndUniversalPartner(assumption, downSet, ignoreSigmas);
        }

        System.out.println("CQ2 mode: LEGACY_SINK (M2 || A)");
        return verifyWithLegacySink(assumption, downSet, ignoreSigmas);
    }

    private Result verifyWithLegacySink(DOTA assumption, Set<String> downSet, Set<String> ignoreSigmas) {
        if (Config.COMPLETED_EXAMPLE) {
            Template assumptionTemplate = VerificationUtil.transToUppaal(assumption, downSet, ignoreSigmas);
            if (portActionMode) {
                VerificationUtil.refinePortActions(assumptionTemplate, true);
            } else {
                VerificationUtil.refine(assumptionTemplate, syncSendMap, false);
            }
            String statement = "A[] not " + assumptionTemplate.getName() + "." + assumptionTemplate.getName() + "sink";
            return verify(m2, assumptionTemplate, null, statement, "nta");
        }

        Template assumptionTemplate = VerificationUtil.transToUppaal(assumption);
        if (portActionMode) {
            VerificationUtil.refinePortActions(assumptionTemplate, true);
        } else {
            VerificationUtil.refine(assumptionTemplate, syncSendMap, false);
        }
        String statement = "A[] not " + assumptionTemplate.getName() + "." + assumptionTemplate.getName() + "sink";
        return verify(m2, assumptionTemplate, ignoreSigmas, statement, "nta");
    }

    private Result verifyWithMirrorAndUniversalPartner(DOTA assumption,
                                                       Set<String> downSet,
                                                       Set<String> ignoreSigmas) {
        Template mirrorAssumption;
        if (Config.COMPLETED_EXAMPLE) {
            mirrorAssumption = VerificationUtil.transToUppaal(assumption, downSet, ignoreSigmas);
        } else {
            mirrorAssumption = VerificationUtil.transToUppaal(assumption);
        }
        VerificationUtil.refinePortActions(mirrorAssumption, true);
        Map<String, String> hookChannelMap = buildHookChannelMap(downSet);
        Declaration cq2Declaration = copyDeclaration(globalDeclaration);
        hookChannelMap = resolveAndInjectHookChannels(cq2Declaration, hookChannelMap);
        rewriteTemplateToHookChannels(mirrorAssumption, hookChannelMap);
        Template universalPartner = buildRelayUniversalPartnerTemplate(downSet, hookChannelMap);

        System.out.println("CQ2 relay hooks: " + hookChannelMap);
        String sinkState = mirrorAssumption.getName() + "sink";
        String statement = "A[] not " + mirrorAssumption.getName() + "." + sinkState;
        Set<String> ntaIgnore = Config.COMPLETED_EXAMPLE ? null : ignoreSigmas;
        return verifyWithExtraTemplates(m2, mirrorAssumption, universalPartner,
                cq2Declaration, ntaIgnore, statement, "nta");
    }

    private Template buildRelayUniversalPartnerTemplate(Set<String> downSet, Map<String, String> hookChannelMap) {
        String templateName = "U_partner";
        UppaalLocation u0 = UppaalModelUtil.buildUppaalLocation(templateName, "u0");
        List<UppaalTransition> transitions = new ArrayList<UppaalTransition>();
        List<UppaalLocation> forwardLocations = new ArrayList<UppaalLocation>();
        List<String> symbols = new ArrayList<String>(downSet);
        Collections.sort(symbols);
        int index = 0;
        for (String symbol : symbols) {
            String normalized = PortActionUtil.normalize(symbol);
            if (!PortActionUtil.isPortAction(normalized)) {
                continue;
            }
            String channel = PortActionUtil.channelOf(normalized);
            String hookChannel = hookChannelMap.get(channel);
            if (hookChannel == null) {
                continue;
            }

            UppaalLocation forward = UppaalModelUtil.buildCommittedUppaalLocation(templateName, "uf" + index);
            forwardLocations.add(forward);
            transitions.add(new UppaalTransitionBuilder(u0, forward)
                    .addSync(PortActionUtil.complement(normalized))
                    .getUppaalTransition());
            transitions.add(new UppaalTransitionBuilder(forward, u0)
                    .addSync(hookChannel + normalized.charAt(normalized.length() - 1))
                    .getUppaalTransition());
            index++;
        }
        TemplateBuilder builder = new TemplateBuilder()
                .setName(templateName)
                .addInitLocation(u0);
        for (UppaalLocation location : forwardLocations) {
            builder.addLocation(location);
        }
        builder.setUppaalTransitionList(transitions);
        return builder.createTemplate();
    }

    private Map<String, String> buildHookChannelMap(Set<String> downSet) {
        Map<String, String> hookMap = new LinkedHashMap<String, String>();
        for (String symbol : downSet) {
            String normalized = PortActionUtil.normalize(symbol);
            if (!PortActionUtil.isPortAction(normalized)) {
                continue;
            }
            String channel = PortActionUtil.channelOf(normalized);
            if (!hookMap.containsKey(channel)) {
                hookMap.put(channel, PortActionUtil.toCq2HookChannel(channel));
            }
        }
        return hookMap;
    }

    private void rewriteTemplateToHookChannels(Template template, Map<String, String> hookMap) {
        if (template == null || template.getUppaalTransitionList() == null || hookMap.isEmpty()) {
            return;
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
            String hook = hookMap.get(channel);
            if (hook == null) {
                continue;
            }
            char suffix = action.charAt(action.length() - 1);
            transition.getSynchronizedLabel().setText(hook + suffix);
        }
    }

    private Declaration copyDeclaration(Declaration declaration) {
        Declaration copied = new Declaration();
        if (declaration == null || !declaration.isMapInitialized()) {
            return copied;
        }
        for (Map.Entry<String, String> entry : declaration.getMap().entrySet()) {
            copied.put(entry.getKey(), entry.getValue());
        }
        return copied;
    }

    private Map<String, String> resolveAndInjectHookChannels(Declaration declaration, Map<String, String> requestedHooks) {
        if (declaration == null || requestedHooks == null || requestedHooks.isEmpty()) {
            return requestedHooks;
        }
        Set<String> declared = new LinkedHashSet<String>();
        if (declaration.isMapInitialized()) {
            for (Map.Entry<String, String> entry : declaration.getMap().entrySet()) {
                String type = entry.getValue();
                if (type == null || !type.contains("chan")) {
                    continue;
                }
                String[] channels = entry.getKey().split(",");
                for (String channel : channels) {
                    String trimmed = channel.trim();
                    if (!trimmed.isEmpty()) {
                        declared.add(trimmed);
                    }
                }
            }
        }

        Map<String, String> resolved = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : requestedHooks.entrySet()) {
            String requested = entry.getValue();
            if (requested == null || requested.trim().isEmpty()) {
                continue;
            }
            String name = requested.trim();
            int idx = 1;
            while (declared.contains(name)) {
                name = requested + "_" + idx;
                idx++;
            }
            declaration.put(name, "chan");
            declared.add(name);
            resolved.put(entry.getKey(), name);
        }
        return resolved;
    }

    public Result isSatisfied(ResetLogicTimeWord ctx, Set<String> downSet) {
        DOTA assumption = VerificationUtil.traceOTA(ctx);
        int beforeLocationCount = assumption.getLocations().size();
        assumption.setSigma(downSet);
        Set<String> baseSigma = portActionMode ? targetSigma : syncSendMap.keySet();
        Set<String> ignoreSigmas = baseSigma.stream().filter(t -> !downSet.contains(t)).collect(Collectors.toSet());

        if (Config.COMPLETED_EXAMPLE) {
            Template assumptionTemplate = VerificationUtil.transToUppaal(assumption, downSet, ignoreSigmas);
            if (portActionMode) {
                VerificationUtil.refinePortActions(assumptionTemplate, true);
            } else {
                VerificationUtil.refine(assumptionTemplate, syncSendMap, false);
            }
            String statement = "E<> " + assumptionTemplate.getName() + "." + assumption.getName() + beforeLocationCount;
            return verify(m2, assumptionTemplate, null, statement, "ctx");
        }

        Template assumptionTemplate = VerificationUtil.transToUppaal(assumption);
        if (portActionMode) {
            VerificationUtil.refinePortActions(assumptionTemplate, true);
        } else {
            VerificationUtil.refine(assumptionTemplate, syncSendMap, false);
        }
        String statement = "E<> " + assumptionTemplate.getName() + "." + assumption.getName() + beforeLocationCount;
        return verify(m2, assumptionTemplate, ignoreSigmas, statement, "ctx");
    }

    @SneakyThrows
    private Result verify(List<Template> m2,
                          Template assumptionTemplate,
                          Set<String> ignoreSigmas,
                          String statement,
                          String ntaName) {
        List<Template> templateList = new ArrayList<Template>(m2);
        templateList.add(assumptionTemplate);
        NTA nta = new NTA(ntaName, globalDeclaration, templateList, ignoreSigmas);
        return Verifyta.isSatisfied(nta, statement);
    }

    @SneakyThrows
    private Result verifyWithExtraTemplates(List<Template> m2,
                                            Template assumptionTemplate,
                                            Template extraTemplate,
                                            Declaration declaration,
                                            Set<String> ignoreSigmas,
                                            String statement,
                                            String ntaName) {
        List<Template> templateList = new ArrayList<Template>(m2);
        templateList.add(extraTemplate);
        templateList.add(assumptionTemplate);
        NTA nta = new NTA(ntaName, declaration, templateList, ignoreSigmas);
        return Verifyta.isSatisfied(nta, statement);
    }
}
