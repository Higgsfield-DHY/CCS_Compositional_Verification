package verification.frame.checkerimpl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import ta.ota.DOTA;
import ta.ota.ResetLogicTimeWord;
import verification.Config;
import verification.frame.Checker;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.NTA;
import verification.uppaal.model.Template;
import verification.uppaal.verify.Result;
import verification.uppaal.verify.Verifyta;
import verification.util.VerificationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class Promise1Checker implements Checker<DOTA> {

    private Declaration globalDeclaration;
    private List<Template> m1;
    private String statement;
    private Map<String, Boolean> syncSendMap;
    private Set<String> targetSigma;
    private boolean portActionMode;

    public Result isSatisfied(DOTA assumption, Set<String> downSet) {
        return isSatisfied(assumption, downSet, "nta");
    }

    public Result isSatisfied(ResetLogicTimeWord ctx, Set<String> downSet) {
        DOTA counterexample = VerificationUtil.traceOTA(ctx);
        return isSatisfied(counterexample, downSet, "ctx");
    }

    private Result isSatisfied(DOTA assumption, Set<String> downSet, String ntaName) {
        assumption = VerificationUtil.removeSink(assumption);
        assumption.setSigma(downSet);
        Set<String> baseSigma = portActionMode ? targetSigma : syncSendMap.keySet();
        Set<String> ignoreSigmas = baseSigma.stream().filter(t -> !downSet.contains(t)).collect(Collectors.toSet());
        if (Config.COMPLETED_EXAMPLE) {
            Template assumptionTemplate = VerificationUtil.transToUppaal(assumption, downSet, ignoreSigmas);
            if (portActionMode) {
                VerificationUtil.refinePortActions(assumptionTemplate, false);
            } else {
                VerificationUtil.refine(assumptionTemplate, syncSendMap, true);
            }
            return verify(m1, assumptionTemplate, null, ntaName);
        } else {
            Template assumptionTemplate = VerificationUtil.transToUppaal(assumption);
            if (portActionMode) {
                VerificationUtil.refinePortActions(assumptionTemplate, false);
            } else {
                VerificationUtil.refine(assumptionTemplate, syncSendMap, true);
            }
            return verify(m1, assumptionTemplate, ignoreSigmas, ntaName);
        }
    }

    public Result isSatisfied(DOTA assumption) {
        assumption = VerificationUtil.removeSink(assumption);
        Set<String> baseSigma = portActionMode ? targetSigma : syncSendMap.keySet();
        Template assumptionTemplate = VerificationUtil.transToUppaal(assumption, baseSigma, null);
        if (portActionMode) {
            VerificationUtil.refinePortActions(assumptionTemplate, false);
        } else {
            VerificationUtil.refine(assumptionTemplate, syncSendMap, true);
        }
        return verify(m1, assumptionTemplate, null, "nta");
    }

    @SneakyThrows
    private Result verify(List<Template> m1, Template assumptionTemplate, Set<String> ignoreSigmas, String ntaName) {
        List<Template> templateList = new ArrayList<>(m1);
        templateList.add(assumptionTemplate);
        NTA nta = new NTA(ntaName, globalDeclaration, templateList, ignoreSigmas);
        return Verifyta.isSatisfied(nta, statement);
    }
}
