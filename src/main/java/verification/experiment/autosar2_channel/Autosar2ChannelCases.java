package verification.experiment.autosar2_channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class Autosar2ChannelCases {
    private Autosar2ChannelCases() {
    }

    public static List<Autosar2ChannelCase> allCases() {
        List<Autosar2ChannelCase> list = new ArrayList<Autosar2ChannelCase>();
        list.add(new Autosar2ChannelCase(
                "Experiment2_1",
                "autosar2.Experiment2_1",
                "M1-single/M2-multi",
                "minimal repartition around buffer1 after original split timeout",
                "buffer1",
                "runnable1..3+buffer2+buffer4+schedule",
                "A[] buffer1.count >= 0",
                false,
                new Autosar2ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment2_1();
                    }
                }));
        list.add(new Autosar2ChannelCase(
                "Experiment2_2",
                "autosar2.Experiment2_2",
                "M1-single/M2-multi",
                "minimal repartition around buffer1 after original split timeout",
                "buffer1",
                "runnable1..3+buffer2+buffer4+schedule",
                "A[] buffer1.count <= buffer1.len",
                false,
                new Autosar2ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment2_2();
                    }
                }));
        list.add(new Autosar2ChannelCase(
                "Experiment2_3",
                "autosar2.Experiment2_3",
                "M1-single/M2-multi",
                "minimal repartition around buffer2 after original split timeout",
                "buffer2",
                "runnable1..3+buffer1+buffer4+rte+schedule",
                "A[] buffer2.count >= 0",
                false,
                new Autosar2ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment2_3();
                    }
                }));
        list.add(new Autosar2ChannelCase(
                "Experiment2_4",
                "autosar2.Experiment2_4",
                "M1-single/M2-multi",
                "minimal repartition around buffer2 after original split timeout",
                "buffer2",
                "runnable1..3+buffer1+buffer4+rte+schedule",
                "A[] buffer2.count <= buffer2.len",
                false,
                new Autosar2ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment2_4();
                    }
                }));
        return Collections.unmodifiableList(list);
    }

    public static Autosar2ChannelCase findById(String caseId) {
        if (caseId == null) {
            return null;
        }
        String key = caseId.trim().toLowerCase(Locale.ROOT);
        for (Autosar2ChannelCase c : allCases()) {
            if (c.normalizedKey().equals(key)) {
                return c;
            }
        }
        return null;
    }

    public static String availableCases() {
        StringBuilder sb = new StringBuilder();
        List<Autosar2ChannelCase> all = allCases();
        for (int i = 0; i < all.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(all.get(i).getCaseId());
        }
        return sb.toString();
    }
}
