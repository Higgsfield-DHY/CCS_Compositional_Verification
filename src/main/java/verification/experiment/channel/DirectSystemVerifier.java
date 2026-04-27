package verification.experiment.channel;

import verification.report.AgVerdict;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.NTA;
import verification.uppaal.model.Template;
import verification.uppaal.verify.Result;
import verification.uppaal.verify.Verifyta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class DirectSystemVerifier {
    private DirectSystemVerifier() {
    }

    public static AgVerdict verify(Declaration globalDeclaration,
                                   List<Template> m1,
                                   List<Template> m2,
                                   String statement) throws IOException {
        List<Template> system = new ArrayList<Template>(m1);
        system.addAll(m2);
        NTA nta = new NTA("direct", globalDeclaration, system);
        Result result = Verifyta.isSatisfied(nta, statement);
        return result.isSatisfy() ? AgVerdict.SAFE : AgVerdict.UNSAFE;
    }
}
