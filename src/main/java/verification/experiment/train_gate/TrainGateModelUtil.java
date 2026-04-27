package verification.experiment.train_gate;

import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.uppaal.model.UppaalLocation;
import verification.uppaal.model.UppaalTransition;
import verification.uppaal.model.builder.TemplateBuilder;
import verification.uppaal.model.builder.UppaalTransitionBuilder;
import verification.util.UppaalModelUtil;

public final class TrainGateModelUtil {
    private TrainGateModelUtil() {
    }

    public static Declaration buildGlobalDeclaration(int queueBound) {
        Declaration declaration = new Declaration();
        declaration.put("N=" + queueBound, "const int");
        declaration.put("el=0", "int[0,N]");
        declaration.put("appr,stop,go,leave,empty,notempty,hd,add,rem", "chan");
        return declaration;
    }

    public static Template buildTrain(int trainId) {
        String name = "Train_" + trainId;
        String clock = "x";
        UppaalLocation safe = UppaalModelUtil.buildUppaalLocation(name, "Safe");
        UppaalLocation appr = UppaalModelUtil.buildUppaalLocation(name, "Appr", clock + "<=20");
        UppaalLocation stop = UppaalModelUtil.buildUppaalLocation(name, "Stop");
        UppaalLocation start = UppaalModelUtil.buildUppaalLocation(name, "Start", clock + "<=15");
        UppaalLocation cross = UppaalModelUtil.buildUppaalLocation(name, "Cross", clock + "<=5");

        UppaalTransition t1 = new UppaalTransitionBuilder(safe, appr)
                .addSync("appr", "!")
                .addAssignment("el=" + trainId + "," + clock + "=0")
                .getUppaalTransition();
        UppaalTransition t2 = new UppaalTransitionBuilder(appr, cross)
                .addGuard(clock + ">=10")
                .addAssignment(clock + "=0")
                .getUppaalTransition();
        UppaalTransition t3 = new UppaalTransitionBuilder(appr, stop)
                .addGuard(clock + "<=10 && el==" + trainId)
                .addSync("stop", "?")
                .addAssignment(clock + "=0")
                .getUppaalTransition();
        UppaalTransition t4 = new UppaalTransitionBuilder(stop, start)
                .addGuard("el==" + trainId)
                .addSync("go", "?")
                .addAssignment(clock + "=0")
                .getUppaalTransition();
        UppaalTransition t5 = new UppaalTransitionBuilder(start, cross)
                .addGuard(clock + ">=7")
                .addAssignment(clock + "=0")
                .getUppaalTransition();
        UppaalTransition t6 = new UppaalTransitionBuilder(cross, safe)
                .addGuard(clock + ">=3")
                .addSync("leave", "!")
                .addAssignment("el=" + trainId + "," + clock + "=0")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addLocalDeclaration(clock, "clock")
                .addInitLocation(safe)
                .addLocations(appr, stop, start, cross)
                .addTransitions(t1, t2, t3, t4, t5, t6)
                .createTemplate();
    }

    public static Template buildGate() {
        String name = "Gate";
        UppaalLocation free = UppaalModelUtil.buildCommittedUppaalLocation(name, "Free");
        UppaalLocation waitFirst = UppaalModelUtil.buildUppaalLocation(name, "WaitFirst");
        UppaalLocation head = UppaalModelUtil.buildCommittedUppaalLocation(name, "Head");
        UppaalLocation send = UppaalModelUtil.buildCommittedUppaalLocation(name, "Send");
        UppaalLocation occ = UppaalModelUtil.buildUppaalLocation(name, "Occ");
        UppaalLocation addEmpty = UppaalModelUtil.buildCommittedUppaalLocation(name, "AddEmpty");
        UppaalLocation stopTrain = UppaalModelUtil.buildCommittedUppaalLocation(name, "StopTrain");
        UppaalLocation addOcc = UppaalModelUtil.buildCommittedUppaalLocation(name, "AddOcc");
        UppaalLocation leaveHook = UppaalModelUtil.buildCommittedUppaalLocation(name, "LeaveHook");

        UppaalTransition t1 = new UppaalTransitionBuilder(free, head)
                .addSync("notempty", "?")
                .getUppaalTransition();
        UppaalTransition t2 = new UppaalTransitionBuilder(free, waitFirst)
                .addSync("empty", "?")
                .getUppaalTransition();
        UppaalTransition t3 = new UppaalTransitionBuilder(head, send)
                .addSync("hd", "!")
                .getUppaalTransition();
        UppaalTransition t4 = new UppaalTransitionBuilder(send, occ)
                .addSync("go", "!")
                .getUppaalTransition();
        UppaalTransition t5 = new UppaalTransitionBuilder(waitFirst, addEmpty)
                .addSync("appr", "?")
                .getUppaalTransition();
        UppaalTransition t6 = new UppaalTransitionBuilder(addEmpty, occ)
                .addSync("add", "!")
                .getUppaalTransition();
        UppaalTransition t7 = new UppaalTransitionBuilder(occ, stopTrain)
                .addSync("appr", "?")
                .getUppaalTransition();
        UppaalTransition t8 = new UppaalTransitionBuilder(stopTrain, addOcc)
                .addSync("stop", "!")
                .getUppaalTransition();
        UppaalTransition t9 = new UppaalTransitionBuilder(addOcc, occ)
                .addSync("add", "!")
                .getUppaalTransition();
        UppaalTransition t10 = new UppaalTransitionBuilder(occ, leaveHook)
                .addSync("leave", "?")
                .getUppaalTransition();
        UppaalTransition t11 = new UppaalTransitionBuilder(leaveHook, free)
                .addSync("rem", "?")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addInitLocation(free)
                .addLocations(waitFirst, head, send, occ, addEmpty, stopTrain, addOcc, leaveHook)
                .addTransitions(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11)
                .createTemplate();
    }

    public static Template buildIntQueue() {
        String name = "Queue";
        UppaalLocation start = UppaalModelUtil.buildUppaalLocation(name, "Start");
        UppaalLocation shift = UppaalModelUtil.buildCommittedUppaalLocation(name, "Shiftdown");

        UppaalTransition t1 = new UppaalTransitionBuilder(start, start)
                .addGuard("len==0")
                .addSync("empty", "!")
                .getUppaalTransition();
        UppaalTransition t2 = new UppaalTransitionBuilder(start, start)
                .addGuard("len>0")
                .addSync("notempty", "!")
                .getUppaalTransition();
        UppaalTransition t3 = new UppaalTransitionBuilder(start, start)
                .addGuard("len < N")
                .addSync("add", "?")
                .addAssignment("list[len]=el,len=len+1")
                .getUppaalTransition();
        UppaalTransition t4 = new UppaalTransitionBuilder(start, start)
                .addSync("hd", "?")
                .addAssignment("el=list[0]")
                .getUppaalTransition();
        UppaalTransition t5 = new UppaalTransitionBuilder(start, shift)
                .addGuard("len>=1")
                .addSync("rem", "!")
                .addAssignment("len=len-1,i=0")
                .getUppaalTransition();
        UppaalTransition t6 = new UppaalTransitionBuilder(shift, shift)
                .addGuard("i<len")
                .addAssignment("list[i]=list[i+1],i=i+1")
                .getUppaalTransition();
        UppaalTransition t7 = new UppaalTransitionBuilder(shift, start)
                .addGuard("len==i")
                .addAssignment("list[i]=0,i=0")
                .getUppaalTransition();

        return new TemplateBuilder()
                .setName(name)
                .addLocalDeclaration("list[N],len=0,i=0", "int[0,N]")
                .addInitLocation(start)
                .addLocations(shift)
                .addTransitions(t1, t2, t3, t4, t5, t6, t7)
                .createTemplate();
    }
}
