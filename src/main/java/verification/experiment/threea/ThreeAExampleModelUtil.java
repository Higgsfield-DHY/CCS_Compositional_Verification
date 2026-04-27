package verification.experiment.threea;

import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.uppaal.model.UppaalLocation;
import verification.uppaal.model.UppaalTransition;
import verification.uppaal.model.builder.TemplateBuilder;
import verification.uppaal.model.builder.UppaalTransitionBuilder;
import verification.util.UppaalModelUtil;

public final class ThreeAExampleModelUtil {
    private ThreeAExampleModelUtil() {
    }

    public static Declaration buildGlobalDeclaration() {
        Declaration declaration = new Declaration();
        declaration.put("x", "clock");
        declaration.put("a", "chan");
        declaration.put("INIT=0", "const int");
        declaration.put("SEEN_IN=1", "const int");
        declaration.put("ERROR=2", "const int");
        declaration.put("obs=INIT", "int");
        return declaration;
    }

    public static Template buildM1Send(String name) {
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        UppaalLocation q1 = UppaalModelUtil.buildUppaalLocation(name, "q1");

        UppaalTransition t1 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a!")
                .addGuard("x>2 && x<=3 && obs==SEEN_IN")
                .addAssignment("x=0,obs=ERROR")
                .getUppaalTransition();
        UppaalTransition t2 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a!")
                .addGuard("x>=1 && x<=2 && obs==SEEN_IN")
                .addAssignment("x=0")
                .getUppaalTransition();
        UppaalTransition t3 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a!")
                .addGuard("x>=1 && x<=3 && obs==INIT")
                .addAssignment("x=0")
                .getUppaalTransition();
        UppaalTransition t4 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a!")
                .addGuard("x>=1 && x<=3 && obs==ERROR")
                .addAssignment("x=0")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocations(q1)
                .addTransitions(t1, t2, t3, t4)
                .createTemplate();
    }

    public static Template buildM1Receive() {
        String name = "M1_R";
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        UppaalLocation q1 = UppaalModelUtil.buildUppaalLocation(name, "q1");

        UppaalTransition t1 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a?")
                .addGuard("x==1 && obs==INIT")
                .addAssignment("x=0,obs=SEEN_IN")
                .getUppaalTransition();
        UppaalTransition t2 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a?")
                .addGuard("x==1 && obs==SEEN_IN")
                .addAssignment("x=0")
                .getUppaalTransition();
        UppaalTransition t3 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a?")
                .addGuard("x==1 && obs==ERROR")
                .addAssignment("x=0")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocations(q1)
                .addTransitions(t1, t2, t3)
                .createTemplate();
    }

    public static Template buildM2Receive(String name) {
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        UppaalLocation q1 = UppaalModelUtil.buildUppaalLocation(name, "q1");

        UppaalTransition t1 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a?")
                .addGuard("x==2")
                .addAssignment("x=0")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocations(q1)
                .addTransitions(t1)
                .createTemplate();
    }

    public static Template buildM2Send() {
        String name = "M2_S";
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        UppaalLocation q1 = UppaalModelUtil.buildUppaalLocation(name, "q1");

        UppaalTransition t1 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a!")
                .addGuard("x==1")
                .addAssignment("x=0")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocations(q1)
                .addTransitions(t1)
                .createTemplate();
    }
}
