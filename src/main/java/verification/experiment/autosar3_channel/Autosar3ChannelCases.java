package verification.experiment.autosar3_channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class Autosar3ChannelCases {
    private Autosar3ChannelCases() {
    }

    public static List<Autosar3ChannelCase> allCases() {
        List<Autosar3ChannelCase> list = new ArrayList<Autosar3ChannelCase>();
        list.add(new Autosar3ChannelCase(
                "Experiment3_1",
                "autosar3.Experiment3_1",
                "M1-multi/M2-single",
                "original split",
                "runnable1..7+buffer1..3+task1..3",
                "schedule",
                "A[] buffer1.count >= 0",
                new Autosar3ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment3_1();
                    }
                }));
        list.add(new Autosar3ChannelCase(
                "Experiment3_2",
                "autosar3.Experiment3_2",
                "M1-single/M2-multi",
                "minimal repartition around buffer2 after original split incompatibility",
                "buffer2",
                "runnable1..7+buffer1+buffer3+task1..3+schedule",
                "A[] buffer2.count >= 0",
                new Autosar3ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment3_2();
                    }
                }));
        list.add(new Autosar3ChannelCase(
                "Experiment3_3",
                "autosar3.Experiment3_3",
                "M1-single/M2-multi",
                "minimal repartition around buffer3 after original split incompatibility",
                "buffer3",
                "runnable1..7+buffer1..2+task1..3+schedule",
                "A[] buffer3.count >= 0",
                new Autosar3ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment3_3();
                    }
                }));
        return Collections.unmodifiableList(list);
    }

    public static Autosar3ChannelCase findById(String caseId) {
        if (caseId == null) {
            return null;
        }
        String key = caseId.trim().toLowerCase(Locale.ROOT);
        for (Autosar3ChannelCase c : allCases()) {
            if (c.normalizedKey().equals(key)) {
                return c;
            }
        }
        return null;
    }

    public static String availableCases() {
        StringBuilder sb = new StringBuilder();
        List<Autosar3ChannelCase> all = allCases();
        for (int i = 0; i < all.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(all.get(i).getCaseId());
        }
        return sb.toString();
    }
}
