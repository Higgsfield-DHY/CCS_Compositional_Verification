package verification.experiment.h16;

import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.uppaal.model.UppaalLocation;
import verification.uppaal.model.UppaalTransition;
import verification.uppaal.model.builder.TemplateBuilder;
import verification.uppaal.model.builder.UppaalTransitionBuilder;
import verification.util.UppaalModelUtil;

public final class H16ModelUtil {
    private H16ModelUtil() {
    }

    public static Declaration buildGlobalDeclaration() {
        return buildGlobalDeclaration("a,b,a_int");
    }

    public static Declaration buildGlobalDeclaration(String channels) {
        Declaration declaration = new Declaration();
        declaration.put("x", "clock");
        declaration.put(channels, "chan");
        declaration.put("INIT=0", "const int");
        declaration.put("SEEN_A=1", "const int");
        declaration.put("SEEN_B=2", "const int");
        declaration.put("OK=3", "const int");
        declaration.put("ERROR=4", "const int");
        declaration.put("obs=INIT", "int");
        return declaration;
    }

    public static Template buildH1() {
        String name = "H1";
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        UppaalLocation q1 = UppaalModelUtil.buildUppaalLocation(name, "q1");

        UppaalTransition t1 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a!")
                .addGuard("x>=1 && x<=3 && obs==INIT")
                .addAssignment("x=0,obs=SEEN_A")
                .getUppaalTransition();
        UppaalTransition t2 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a!")
                .addGuard("x>=1 && x<=3 && obs==SEEN_B")
                .addAssignment("x=0,obs=ERROR")
                .getUppaalTransition();
        UppaalTransition t3 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a!")
                .addGuard("x>=1 && x<=3 && obs==SEEN_A")
                .addAssignment("x=0")
                .getUppaalTransition();
        UppaalTransition t4 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a!")
                .addGuard("x>=1 && x<=3 && obs==OK")
                .addAssignment("x=0")
                .getUppaalTransition();
        UppaalTransition t5 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a!")
                .addGuard("x>=1 && x<=3 && obs==ERROR")
                .addAssignment("x=0")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocations(q1)
                .addTransitions(t1, t2, t3, t4, t5)
                .createTemplate();
    }

    public static Template buildH2() {
        String name = "H2";
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        UppaalLocation q1 = UppaalModelUtil.buildUppaalLocation(name, "q1");

        UppaalTransition t1 = new UppaalTransitionBuilder(q0, q1)
                .addSync("b?")
                .addGuard("x>2 && x<=3 && obs==INIT")
                .addAssignment("obs=SEEN_B")
                .getUppaalTransition();
        UppaalTransition t2 = new UppaalTransitionBuilder(q0, q1)
                .addSync("b?")
                .addGuard("x>2 && x<=3 && obs==SEEN_A")
                .addAssignment("obs=OK")
                .getUppaalTransition();
        UppaalTransition t3 = new UppaalTransitionBuilder(q0, q1)
                .addSync("b?")
                .addGuard("x>2 && x<=3 && obs==SEEN_B")
                .getUppaalTransition();
        UppaalTransition t4 = new UppaalTransitionBuilder(q0, q1)
                .addSync("b?")
                .addGuard("x>2 && x<=3 && obs==OK")
                .getUppaalTransition();
        UppaalTransition t5 = new UppaalTransitionBuilder(q0, q1)
                .addSync("b?")
                .addGuard("x>2 && x<=3 && obs==ERROR")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocations(q1)
                .addTransitions(t1, t2, t3, t4, t5)
                .createTemplate();
    }

    public static Template buildH3() {
        return buildH3("x>=1 && x<=2");
    }

    public static Template buildH3(String guard) {
        String name = "H3";
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        UppaalLocation q1 = UppaalModelUtil.buildUppaalLocation(name, "q1");

        UppaalTransition t1 = new UppaalTransitionBuilder(q0, q1)
                .addSync("a?")
                .addGuard(guard)
                .addAssignment("x=0")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocations(q1)
                .addTransitions(t1)
                .createTemplate();
    }

    public static Template buildH4() {
        return buildH4("x>2 && x<=3");
    }

    public static Template buildH4(String guard) {
        String name = "H4";
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        UppaalLocation q1 = UppaalModelUtil.buildUppaalLocation(name, "q1");

        UppaalTransition t1 = new UppaalTransitionBuilder(q0, q1)
                .addSync("b!")
                .addGuard(guard)
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocations(q1)
                .addTransitions(t1)
                .createTemplate();
    }

    public static Template buildH5() {
        return buildH5("a", "x>=1 && x<=2");
    }

    public static Template buildH5(String channel, String guard) {
        String name = "H5";
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        UppaalLocation q1 = UppaalModelUtil.buildUppaalLocation(name, "q1");

        UppaalTransition t1 = new UppaalTransitionBuilder(q0, q1)
                .addSync(channel + "!")
                .addGuard(guard)
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocations(q1)
                .addTransitions(t1)
                .createTemplate();
    }

    public static Template buildH6() {
        return buildH6("a", "x>=1 && x<=2");
    }

    public static Template buildH6(String channel, String guard) {
        String name = "H6";
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        UppaalLocation q1 = UppaalModelUtil.buildUppaalLocation(name, "q1");

        UppaalTransition t1 = new UppaalTransitionBuilder(q0, q1)
                .addSync(channel + "?")
                .addGuard(guard)
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(q0)
                .addLocations(q1)
                .addTransitions(t1)
                .createTemplate();
    }
}
