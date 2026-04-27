package verification.experiment.fischer_channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class FischerChannelCases {
    private FischerChannelCases() {
    }

    public static List<FischerChannelCase> allCases() {
        List<FischerChannelCase> list = new ArrayList<FischerChannelCase>();
        list.add(new FischerChannelCase(
                "Fischer2_1",
                "fischer.FischerTa3Source",
                "M1-single/M2-multi",
                "monitor-only partition",
                "Mutex",
                "Process_1+Process_2+Global_Var",
                "A[] Mutex.hold <= 1",
                new FischerChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Fischer2_1();
                    }
                }));
        list.add(new FischerChannelCase(
                "Fischer2_2",
                "fischer.FischerTa3Source",
                "M1-multi/M2-multi",
                "Process_1 internal to M1",
                "Mutex+Process_1",
                "Process_2+Global_Var",
                "A[] Mutex.hold <= 1",
                new FischerChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Fischer2_2();
                    }
                }));
        list.add(new FischerChannelCase(
                "Fischer3_1",
                "fischer.FischerTa4Source",
                "M1-single/M2-multi",
                "monitor-only partition",
                "Mutex",
                "Process_1+Process_2+Process_3+Global_Var",
                "A[] Mutex.hold <= 1",
                new FischerChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Fischer3_1();
                    }
                }));
        list.add(new FischerChannelCase(
                "Fischer3_2",
                "fischer.FischerTa4Source",
                "M1-multi/M2-multi",
                "Process_1 internal to M1",
                "Mutex+Process_1",
                "Process_2+Process_3+Global_Var",
                "A[] Mutex.hold <= 1",
                new FischerChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Fischer3_2();
                    }
                }));
        return Collections.unmodifiableList(list);
    }

    public static FischerChannelCase findById(String caseId) {
        if (caseId == null) {
            return null;
        }
        String key = caseId.trim().toLowerCase(Locale.ROOT);
        for (FischerChannelCase c : allCases()) {
            if (c.normalizedKey().equals(key)) {
                return c;
            }
        }
        return null;
    }

    public static String availableCases() {
        StringBuilder sb = new StringBuilder();
        List<FischerChannelCase> all = allCases();
        for (int i = 0; i < all.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(all.get(i).getCaseId());
        }
        return sb.toString();
    }
}
