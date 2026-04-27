package verification.experiment.pc_channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class PcChannelCases {
    private PcChannelCases() {
    }

    public static List<PcChannelCase> allCases() {
        List<PcChannelCase> list = new ArrayList<PcChannelCase>();
        list.add(new PcChannelCase(
                "PC_1",
                "pc.PcP1C3K2Source",
                "M1-single/M2-multi",
                "binary-channel adaptation",
                "Buffer",
                "Producer_1+Consumer_1+Consumer_2+Consumer_3",
                "A[] Buffer.count >= 0 && Buffer.count <= Buffer.len",
                new PcChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new PC_1();
                    }
                }));
        list.add(new PcChannelCase(
                "PC_2",
                "pc.PcP1C3K2Source",
                "M1-multi/M2-multi",
                "producer internal to M1",
                "Buffer+Producer_1",
                "Consumer_1+Consumer_2+Consumer_3",
                "A[] Buffer.count >= 0 && Buffer.count <= Buffer.len",
                new PcChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new PC_2();
                    }
                }));
        list.add(new PcChannelCase(
                "PC_3",
                "pc.PcP1C3K2Source",
                "M1-multi/M2-single",
                "all consumers internal to M1",
                "Buffer+Consumer_1+Consumer_2+Consumer_3",
                "Producer_1",
                "A[] Buffer.count >= 0 && Buffer.count <= Buffer.len",
                new PcChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new PC_3();
                    }
                }));
        return Collections.unmodifiableList(list);
    }

    public static PcChannelCase findById(String caseId) {
        if (caseId == null) {
            return null;
        }
        String key = caseId.trim().toLowerCase(Locale.ROOT);
        for (PcChannelCase c : allCases()) {
            if (c.normalizedKey().equals(key)) {
                return c;
            }
        }
        return null;
    }

    public static String availableCases() {
        StringBuilder sb = new StringBuilder();
        List<PcChannelCase> all = allCases();
        for (int i = 0; i < all.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(all.get(i).getCaseId());
        }
        return sb.toString();
    }
}
