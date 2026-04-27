package verification.experiment.fischer;

import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.uppaal.model.UppaalLocation;
import verification.uppaal.model.UppaalTransition;
import verification.uppaal.model.builder.TemplateBuilder;
import verification.uppaal.model.builder.UppaalTransitionBuilder;
import verification.util.UppaalModelUtil;

public final class FischerModelUtil {
    private FischerModelUtil() {
    }

    public static Declaration buildGlobalDeclaration(int processCount) {
        Declaration declaration = new Declaration();
        StringBuilder channels = new StringBuilder();
        for (int i = 1; i <= processCount; i++) {
            append(channels, tryChannel(i));
            append(channels, setChannel(i));
            append(channels, enterChannel(i));
            append(channels, exitChannel(i));
            append(channels, retryChannel(i));
            append(channels, enterHookChannel(i));
            append(channels, exitHookChannel(i));
        }
        declaration.put(channels.toString(), "chan");
        return declaration;
    }

    public static Template buildMutexMonitor(int processCount) {
        String name = "Mutex";
        UppaalLocation q0 = UppaalModelUtil.buildUppaalLocation(name, "q0");
        TemplateBuilder builder = new TemplateBuilder()
                .setName(name)
                .addLocalDeclaration("hold=0", "int")
                .addInitLocation(q0);
        for (int i = 1; i <= processCount; i++) {
            builder.addTransition(new UppaalTransitionBuilder(q0, q0)
                    .addSync(enterHookChannel(i), "?")
                    .addAssignment("hold = hold + 1")
                    .getUppaalTransition());
            builder.addTransition(new UppaalTransitionBuilder(q0, q0)
                    .addSync(exitHookChannel(i), "?")
                    .addGuard("hold > 0")
                    .addAssignment("hold = hold - 1")
                    .getUppaalTransition());
        }
        return builder.createTemplate();
    }

    public static Template buildTa3Process(int index) {
        return buildProcess(index, 5, 10);
    }

    public static Template buildTa4Process(int index) {
        return buildProcess(index, 0, 10);
    }

    public static Template buildGlobalVar(int processCount) {
        String name = "Global_Var";
        UppaalLocation[] states = new UppaalLocation[processCount + 1];
        states[0] = UppaalModelUtil.buildUppaalLocation(name, "L0");
        TemplateBuilder builder = new TemplateBuilder()
                .setName(name)
                .addLocalDeclaration("y", "clock")
                .addInitLocation(states[0]);
        for (int i = 1; i <= processCount; i++) {
            states[i] = UppaalModelUtil.buildUppaalLocation(name, "L" + i);
            builder.addLocation(states[i]);
        }

        for (int i = 1; i <= processCount; i++) {
            UppaalLocation enterHook = UppaalModelUtil.buildCommittedUppaalLocation(name, "enterHook" + i);
            UppaalLocation exitHook = UppaalModelUtil.buildCommittedUppaalLocation(name, "exitHook" + i);
            builder.addLocations(enterHook, exitHook);

            builder.addTransition(mainTransition(states[0], states[0], tryChannel(i), "y = 0"));
            builder.addTransition(mainTransition(states[0], states[0], retryChannel(i), "y = 0"));
            builder.addTransition(mainTransition(states[0], states[i], setChannel(i), "y = 0"));

            builder.addTransition(mainTransition(states[i], states[i], setChannel(i), "y = 0"));
            builder.addTransition(mainTransition(states[i], enterHook, enterChannel(i), "y = 0"));
            builder.addTransition(mainTransition(states[i], exitHook, exitChannel(i), "y = 0"));
            builder.addTransition(new UppaalTransitionBuilder(enterHook, states[i])
                    .addSync(enterHookChannel(i), "!")
                    .getUppaalTransition());
            builder.addTransition(new UppaalTransitionBuilder(exitHook, states[0])
                    .addSync(exitHookChannel(i), "!")
                    .getUppaalTransition());

            for (int j = 1; j <= processCount; j++) {
                if (j == i) {
                    continue;
                }
                builder.addTransition(mainTransition(states[i], states[i], retryChannel(j), "y = 0"));
                builder.addTransition(mainTransition(states[i], states[j], setChannel(j), "y = 0"));
            }
        }
        return builder.createTemplate();
    }

    public static String tryChannel(int index) {
        return "try_" + index;
    }

    public static String setChannel(int index) {
        return "setX_" + index;
    }

    public static String enterChannel(int index) {
        return "enter_" + index;
    }

    public static String exitChannel(int index) {
        return "setX0_" + index;
    }

    public static String retryChannel(int index) {
        return "retry_" + index;
    }

    public static String enterHookChannel(int index) {
        return "enter_hook_" + index;
    }

    public static String exitHookChannel(int index) {
        return "exit_hook_" + index;
    }

    private static Template buildProcess(int index, int setLow, int setHigh) {
        String name = "Process_" + index;
        String clock = "x" + index;
        UppaalLocation idle = UppaalModelUtil.buildUppaalLocation(name, "idle");
        UppaalLocation req = UppaalModelUtil.buildUppaalLocation(name, "req");
        UppaalLocation wait = UppaalModelUtil.buildUppaalLocation(name, "wait");
        UppaalLocation crit = UppaalModelUtil.buildUppaalLocation(name, "crit");

        UppaalTransition t1 = new UppaalTransitionBuilder(idle, req)
                .addSync(tryChannel(index), "!")
                .addAssignment(clock + " = 0")
                .getUppaalTransition();
        UppaalTransition t2 = new UppaalTransitionBuilder(req, wait)
                .addSync(setChannel(index), "!")
                .addGuard(clock + " >= " + setLow + " && " + clock + " < " + setHigh)
                .addAssignment(clock + " = 0")
                .getUppaalTransition();
        UppaalTransition t3 = new UppaalTransitionBuilder(wait, crit)
                .addSync(enterChannel(index), "!")
                .addGuard(clock + " > 19")
                .getUppaalTransition();
        UppaalTransition t4 = new UppaalTransitionBuilder(crit, idle)
                .addSync(exitChannel(index), "!")
                .getUppaalTransition();
        UppaalTransition t5 = new UppaalTransitionBuilder(wait, idle)
                .addSync(retryChannel(index), "!")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addLocalDeclaration(clock, "clock")
                .addInitLocation(idle)
                .addLocations(req, wait, crit)
                .addTransitions(t1, t2, t3, t4, t5)
                .createTemplate();
    }

    private static UppaalTransition mainTransition(UppaalLocation from, UppaalLocation to,
                                                   String channel, String assignment) {
        return new UppaalTransitionBuilder(from, to)
                .addSync(channel, "?")
                .addAssignment(assignment)
                .getUppaalTransition();
    }

    private static void append(StringBuilder builder, String token) {
        if (builder.length() > 0) {
            builder.append(',');
        }
        builder.append(token);
    }
}
