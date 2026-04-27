package verification.experiment.pc;

import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.uppaal.model.UppaalLocation;
import verification.uppaal.model.UppaalTransition;
import verification.uppaal.model.builder.TemplateBuilder;
import verification.uppaal.model.builder.UppaalTransitionBuilder;
import verification.util.UppaalModelUtil;

public final class PcModelUtil {
    private PcModelUtil() {
    }

    public static Declaration buildGlobalDeclaration() {
        Declaration declaration = new Declaration();
        declaration.put("put,get", "chan");
        return declaration;
    }

    public static Template buildBuffer() {
        String name = "Buffer";
        UppaalLocation b0 = UppaalModelUtil.buildUppaalLocation(name, "B0");
        UppaalLocation b1 = UppaalModelUtil.buildUppaalLocation(name, "B1");
        UppaalLocation b2 = UppaalModelUtil.buildUppaalLocation(name, "B2");

        UppaalTransition t1 = new UppaalTransitionBuilder(b0, b1)
                .addSync("put", "?")
                .addAssignment("count = count + 1")
                .getUppaalTransition();
        UppaalTransition t2 = new UppaalTransitionBuilder(b1, b2)
                .addSync("put", "?")
                .addAssignment("count = count + 1")
                .getUppaalTransition();
        UppaalTransition t3 = new UppaalTransitionBuilder(b1, b0)
                .addSync("get", "?")
                .addAssignment("count = count - 1")
                .getUppaalTransition();
        UppaalTransition t4 = new UppaalTransitionBuilder(b2, b1)
                .addSync("get", "?")
                .addAssignment("count = count - 1")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addLocalDeclaration("count=0,len=2", "int")
                .addInitLocation(b0)
                .addLocations(b1, b2)
                .addTransitions(t1, t2, t3, t4)
                .createTemplate();
    }

    public static Template buildProducer1() {
        String name = "Producer_1";
        String clock = "xp1";
        UppaalLocation idle = UppaalModelUtil.buildUppaalLocation(name, "Idle");
        UppaalLocation ready = UppaalModelUtil.buildUppaalLocation(name, "Ready");

        UppaalTransition produce = new UppaalTransitionBuilder(idle, ready)
                .addGuard(clock + " < 5")
                .addAssignment(clock + " = 0")
                .getUppaalTransition();
        UppaalTransition put = new UppaalTransitionBuilder(ready, idle)
                .addSync("put", "!")
                .addGuard(clock + " < 2")
                .addAssignment(clock + " = 0")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addLocalDeclaration(clock, "clock")
                .addInitLocation(idle)
                .addLocations(ready)
                .addTransitions(produce, put)
                .createTemplate();
    }

    public static Template buildConsumer1() {
        return buildConsumer("Consumer_1", "yc1");
    }

    public static Template buildConsumer2() {
        return buildConsumer("Consumer_2", "yc2");
    }

    public static Template buildConsumer3() {
        return buildConsumer("Consumer_3", "yc3");
    }

    private static Template buildConsumer(String name, String clock) {
        UppaalLocation wait = UppaalModelUtil.buildUppaalLocation(name, "Wait");
        UppaalLocation use = UppaalModelUtil.buildUppaalLocation(name, "Use");

        UppaalTransition get = new UppaalTransitionBuilder(wait, use)
                .addSync("get", "!")
                .addGuard(clock + " < 6")
                .addAssignment(clock + " = 0")
                .getUppaalTransition();
        UppaalTransition consume = new UppaalTransitionBuilder(use, wait)
                .addGuard(clock + " >= 1 && " + clock + " < 4")
                .addAssignment(clock + " = 0")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addLocalDeclaration(clock, "clock")
                .addInitLocation(wait)
                .addLocations(use)
                .addTransitions(get, consume)
                .createTemplate();
    }
}
