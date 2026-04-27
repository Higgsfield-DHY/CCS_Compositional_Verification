package verification.experiment.autosar1_channel;

import verification.experiment.autosar1_channel.mutileM1.Experiment1_1;
import verification.experiment.autosar1_channel.mutileM1.Experiment1_2;
import verification.experiment.autosar1_channel.mutileM1.Experiment1_3;
import verification.experiment.autosar1_channel.mutileM1.Experiment1_4;
import verification.experiment.autosar1_channel.mutileM1.Experiment1_5;
import verification.experiment.autosar1_channel.mutileM1.Experiment1_6;
import verification.experiment.autosar1_channel.singleM1.Experiment1_single_1;
import verification.experiment.autosar1_channel.singleM1.Experiment1_single_2;
import verification.experiment.autosar1_channel.singleM1.Experiment1_single_3;
import verification.experiment.autosar1_channel.singleM1.Experiment1_single_4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class Autosar1ChannelCases {
    private Autosar1ChannelCases() {
    }

    public static List<Autosar1ChannelCase> allCases() {
        List<Autosar1ChannelCase> list = new ArrayList<Autosar1ChannelCase>();

        list.add(new Autosar1ChannelCase(
                "Experiment1_single_1",
                "autosar1.singleM1.Experiment1_single_1",
                "M1-single/M2-multi",
                "original split",
                "buffer1",
                "runnable1+runnable2+runnable3+runnable4+buffer2+schedule2+schedule1",
                "A[] (buffer1.count >=0)",
                new Autosar1ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment1_single_1();
                    }
                }));
        list.add(new Autosar1ChannelCase(
                "Experiment1_single_2",
                "autosar1.singleM1.Experiment1_single_2",
                "M1-single/M2-multi",
                "original split",
                "buffer1",
                "runnable1+runnable2+runnable3+runnable4+buffer2+schedule2+schedule1",
                "A[] buffer1.count <= buffer1.len",
                new Autosar1ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment1_single_2();
                    }
                }));
        list.add(new Autosar1ChannelCase(
                "Experiment1_single_3",
                "autosar1.singleM1.Experiment1_single_3",
                "M1-single/M2-multi",
                "original split",
                "buffer2",
                "runnable1+runnable2+runnable3+runnable4+buffer1+schedule2+schedule1",
                "A[] (buffer2.count >=0)",
                new Autosar1ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment1_single_3();
                    }
                }));
        list.add(new Autosar1ChannelCase(
                "Experiment1_single_4",
                "autosar1.singleM1.Experiment1_single_4",
                "M1-single/M2-multi",
                "original split",
                "buffer2",
                "runnable1+runnable2+runnable3+runnable4+buffer1+schedule2+schedule1",
                "A[] buffer2.count <= buffer2.len",
                new Autosar1ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment1_single_4();
                    }
                }));

        list.add(new Autosar1ChannelCase(
                "Experiment1_1",
                "autosar1.mutileM1.Experiment1_1",
                "M1-multi/M2-single",
                "original split",
                "runnable1+runnable2+runnable3+runnable4+buffer1+buffer2+schedule2",
                "schedule1",
                "A[] (buffer1.count >=0)",
                new Autosar1ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment1_1();
                    }
                }));
        list.add(new Autosar1ChannelCase(
                "Experiment1_2",
                "autosar1.mutileM1.Experiment1_2",
                "M1-multi/M2-single",
                "original split",
                "runnable1+runnable2+runnable3+runnable4+buffer1+buffer2+schedule2",
                "schedule1",
                "A[] (buffer1.count <= buffer1.len)",
                new Autosar1ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment1_2();
                    }
                }));
        list.add(new Autosar1ChannelCase(
                "Experiment1_3",
                "autosar1.mutileM1.Experiment1_3",
                "M1-multi/M2-single",
                "original split",
                "runnable1+runnable2+runnable3+runnable4+buffer1+buffer2+schedule2",
                "schedule1",
                "A[] (buffer2.count >=0)",
                new Autosar1ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment1_3();
                    }
                }));
        list.add(new Autosar1ChannelCase(
                "Experiment1_4",
                "autosar1.mutileM1.Experiment1_4",
                "M1-multi/M2-single",
                "original split",
                "runnable1+runnable2+runnable3+runnable4+buffer1+buffer2+schedule2",
                "schedule1",
                "A[] (buffer2.count <= buffer2.len)",
                new Autosar1ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment1_4();
                    }
                }));
        list.add(new Autosar1ChannelCase(
                "Experiment1_5",
                "autosar1.mutileM1.Experiment1_5",
                "M1-multi/M2-single",
                "original split",
                "runnable1+runnable2+runnable3+runnable4+buffer1+buffer2+schedule2",
                "schedule1",
                "A[] not (runnable1.s2 and runnable2.s2)",
                new Autosar1ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment1_5();
                    }
                }));
        list.add(new Autosar1ChannelCase(
                "Experiment1_6",
                "autosar1.mutileM1.Experiment1_6",
                "M1-multi/M2-single",
                "original split",
                "runnable1+runnable2+runnable3+runnable4+buffer1+buffer2+schedule1",
                "schedule2",
                "A[] not (runnable3.s2 and runnable4.s2)",
                new Autosar1ChannelCase.CaseFactory() {
                    @Override
                    public verification.experiment.Experiment create() {
                        return new Experiment1_6();
                    }
                }));

        return Collections.unmodifiableList(list);
    }

    public static Autosar1ChannelCase findById(String caseId) {
        if (caseId == null) {
            return null;
        }
        String key = caseId.trim().toLowerCase(Locale.ROOT);
        for (Autosar1ChannelCase c : allCases()) {
            if (c.normalizedKey().equals(key)) {
                return c;
            }
        }
        return null;
    }

    public static String availableCases() {
        StringBuilder sb = new StringBuilder();
        List<Autosar1ChannelCase> all = allCases();
        for (int i = 0; i < all.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(all.get(i).getCaseId());
        }
        return sb.toString();
    }
}
