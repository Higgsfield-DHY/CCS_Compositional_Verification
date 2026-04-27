package verification.experiment.train_gate_channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class TrainGateChannelCases {
    private TrainGateChannelCases() {
    }

    public static List<TrainGateChannelCase> allCases() {
        List<TrainGateChannelCase> list = new ArrayList<TrainGateChannelCase>();
        list.add(new TrainGateChannelCase(
                "TrainGate_1",
                "train_gate.TrainGateN7Source",
                "M1-central/M2-symmetric",
                "Gate+Queue internal to M1",
                "Gate+Queue",
                "Train_1+Train_2+Train_3+Train_4+Train_5+Train_6+Train_7",
                "A[] Queue.list[N-1] == 0",
                new TrainGateChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new TrainGate_1();
                    }
                }));
        list.add(new TrainGateChannelCase(
                "TrainGate_2",
                "train_gate.TrainGateN8Source",
                "M1-central/M2-symmetric",
                "Gate+Queue internal to M1",
                "Gate+Queue",
                "Train_1+Train_2+Train_3+Train_4+Train_5+Train_6+Train_7+Train_8",
                "A[] Queue.list[N-1] == 0",
                new TrainGateChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new TrainGate_2();
                    }
                }));
        list.add(new TrainGateChannelCase(
                "TrainGate_3",
                "train_gate.TrainGateN9Source",
                "M1-central/M2-symmetric",
                "Gate+Queue internal to M1",
                "Gate+Queue",
                "Train_1+Train_2+Train_3+Train_4+Train_5+Train_6+Train_7+Train_8+Train_9",
                "A[] Queue.list[N-1] == 0",
                new TrainGateChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new TrainGate_3();
                    }
                }));
        return Collections.unmodifiableList(list);
    }

    public static TrainGateChannelCase findById(String caseId) {
        if (caseId == null) {
            return null;
        }
        String key = caseId.trim().toLowerCase(Locale.ROOT);
        for (TrainGateChannelCase c : allCases()) {
            if (c.normalizedKey().equals(key)) {
                return c;
            }
        }
        return null;
    }

    public static String availableCases() {
        StringBuilder sb = new StringBuilder();
        List<TrainGateChannelCase> all = allCases();
        for (int i = 0; i < all.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(all.get(i).getCaseId());
        }
        return sb.toString();
    }
}
