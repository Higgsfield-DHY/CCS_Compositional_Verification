package verification.experiment.autosar2_channel;

import verification.uppaal.model.Template;
import verification.uppaal.model.UppaalTransition;
import verification.uppaal.model.label.AssignmentLabel;
import verification.uppaal.model.label.GuardLabel;
import verification.uppaal.model.label.SelectLabel;
import verification.uppaal.model.label.SynchronizedLabel;
import verification.util.PortActionUtil;

import java.util.ArrayList;
import java.util.List;

public final class Autosar2ChannelSplitUtil {
    private Autosar2ChannelSplitUtil() {
    }

    public static void applyM1Split(List<Template> m1Templates) {
        if (m1Templates == null) {
            return;
        }
        for (Template template : m1Templates) {
            if ("rte".equals(template.getName())) {
                renameSync(template, "read2!", "read2_int!");
            } else if ("buffer2".equals(template.getName())) {
                splitBuffer2Read2(template);
            }
        }
    }

    public static void applyM2Split(List<Template> m2Templates) {
        if (m2Templates == null) {
            return;
        }
        for (Template template : m2Templates) {
            if ("runnable3".equals(template.getName())) {
                renameSync(template, "read2!", "read2_ext!");
            }
        }
    }

    private static void renameSync(Template template, String from, String to) {
        if (template.getUppaalTransitionList() == null) {
            return;
        }
        for (UppaalTransition transition : template.getUppaalTransitionList()) {
            if (transition.getSynchronizedLabel() == null || transition.getSynchronizedLabel().getText() == null) {
                continue;
            }
            String sync = PortActionUtil.normalize(transition.getSynchronizedLabel().getText());
            if (from.equals(sync)) {
                transition.getSynchronizedLabel().setText(to);
            }
        }
    }

    private static void splitBuffer2Read2(Template template) {
        if (template.getUppaalTransitionList() == null) {
            return;
        }
        List<UppaalTransition> updated = new ArrayList<UppaalTransition>(template.getUppaalTransitionList());
        List<UppaalTransition> extra = new ArrayList<UppaalTransition>();
        for (UppaalTransition transition : updated) {
            if (transition.getSynchronizedLabel() == null || transition.getSynchronizedLabel().getText() == null) {
                continue;
            }
            String sync = PortActionUtil.normalize(transition.getSynchronizedLabel().getText());
            if (!"read2?".equals(sync)) {
                continue;
            }
            transition.getSynchronizedLabel().setText("read2_int?");
            UppaalTransition copy = copyTransition(transition);
            if (copy.getSynchronizedLabel() != null) {
                copy.getSynchronizedLabel().setText("read2_ext?");
                extra.add(copy);
            }
        }
        updated.addAll(extra);
        template.setUppaalTransitionList(updated);
    }

    private static UppaalTransition copyTransition(UppaalTransition original) {
        UppaalTransition copy = new UppaalTransition(original.getSource(), original.getTarget());
        if (original.getSelectLabel() != null && original.getSelectLabel().getText() != null) {
            copy.setSelectLabel(new SelectLabel(original.getSelectLabel().getText()));
        }
        if (original.getGuardLabel() != null && original.getGuardLabel().getText() != null) {
            GuardLabel guard = new GuardLabel();
            guard.setText(original.getGuardLabel().getText());
            copy.setGuardLabel(guard);
        }
        if (original.getSynchronizedLabel() != null && original.getSynchronizedLabel().getText() != null) {
            SynchronizedLabel sync = new SynchronizedLabel();
            sync.setText(original.getSynchronizedLabel().getText());
            copy.setSynchronizedLabel(sync);
        }
        if (original.getAssignmentLabel() != null && original.getAssignmentLabel().getText() != null) {
            AssignmentLabel assignment = new AssignmentLabel();
            assignment.setText(original.getAssignmentLabel().getText());
            copy.setAssignmentLabel(assignment);
        }
        return copy;
    }
}

