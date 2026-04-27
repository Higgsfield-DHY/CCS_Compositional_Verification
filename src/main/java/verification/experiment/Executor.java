package verification.experiment;

import verification.experiment.autosar1.mutileM1.Experiment1_1;
import verification.experiment.autosar1.mutileM1.Experiment1_2;
import verification.experiment.autosar1.mutileM1.Experiment1_3;
import verification.experiment.autosar1.mutileM1.Experiment1_4;
import verification.experiment.autosar1.mutileM1.Experiment1_5;
import verification.experiment.autosar1.mutileM1.Experiment1_6;
import verification.experiment.autosar1.singleM1.Experiment1_single_1;
import verification.experiment.autosar1.singleM1.Experiment1_single_2;
import verification.experiment.autosar1.singleM1.Experiment1_single_3;
import verification.experiment.autosar1.singleM1.Experiment1_single_4;
import verification.experiment.autosar1_channel.Autosar1ChannelBatchExecutor;
import verification.experiment.autosar1_channel.Autosar1ChannelRunner;
import verification.experiment.autosar2.*;
import verification.experiment.autosar2_channel.Autosar2ChannelBatchExecutor;
import verification.experiment.autosar2_channel.Autosar2ChannelRunner;
import verification.experiment.autosar3.Experiment3_1;
import verification.experiment.autosar3.Experiment3_2;
import verification.experiment.autosar3.Experiment3_3;
import verification.experiment.autosar3_channel.Autosar3ChannelBatchExecutor;
import verification.experiment.autosar3_channel.Autosar3ChannelRunner;
import verification.experiment.h16.H16Experiment;
import verification.experiment.threea.ThreeARunner;
import verification.experiment.validation.Autosar1PairCheckRunner;
import verification.experiment.validation.Autosar2PairCheckRunner;
import verification.experiment.validation.Autosar3PairCheckRunner;

import java.io.IOException;

public class Executor {
    public static void main(String[] args) throws IOException {
        // NOTE:
        // Channel experiments now have a dedicated entry:
        // verification.experiment.ChannelExecutor
        // Use that class for direct-only / ag-only / compare / summary runs.

        // ==================== AUTOSAR-1 Channel ====================
        String autosar1ChannelCaseId = null;
        // autosar1ChannelCaseId = "Experiment1_single_1";
        // autosar1ChannelCaseId = "Experiment1_single_2";
        // autosar1ChannelCaseId = "Experiment1_single_3";
        // autosar1ChannelCaseId = "Experiment1_single_4";
//         autosar1ChannelCaseId = "Experiment1_1";
        // autosar1ChannelCaseId = "Experiment1_2";
        // autosar1ChannelCaseId = "Experiment1_3";
        // autosar1ChannelCaseId = "Experiment1_4";
        // autosar1ChannelCaseId = "Experiment1_5";
        // autosar1ChannelCaseId = "Experiment1_6";

        // Autosar1ChannelBatchExecutor.main(new String[]{"--all", "--timeout-ms=600000"});
        // return;

        // ==================== AUTOSAR-2 Channel ====================
        String autosar2ChannelCaseId = null;
//         autosar2ChannelCaseId = "Experiment2_1";
//         autosar2ChannelCaseId = "Experiment2_2";
        // autosar2ChannelCaseId = "Experiment2_3";
        // autosar2ChannelCaseId = "Experiment2_4";

        // Autosar2ChannelBatchExecutor.main(new String[]{"--all", "--timeout-ms=600000"});
        // return;

        // ==================== AUTOSAR-3 Channel ====================
        String autosar3ChannelCaseId = null;
//         autosar3ChannelCaseId = "Experiment3_1";
//         autosar3ChannelCaseId = "Experiment3_2";
        // autosar3ChannelCaseId = "Experiment3_3";

        // Autosar3ChannelBatchExecutor.main(new String[]{"--all", "--timeout-ms=600000"});
        // return;

        // ==================== AUTOSAR Channel All Batches ====================
        boolean runAllAutosarChannelBatches = false;
        // runAllAutosarChannelBatches = true;

        // ==================== 3a3a (Bidirectional Domain Split) ====================
        boolean runThreeA = false;
        // runThreeA = true;

        // ==================== Pair Check (strict mirror) ====================
        // Autosar1PairCheckRunner.main(new String[0]);
        // return;
        // Autosar2PairCheckRunner.main(new String[0]);
        // return;
        // Autosar3PairCheckRunner.main(new String[0]);
        // return;

        if (autosar1ChannelCaseId != null) {
            System.out.println("Running experiment: AUTOSAR-1 Channel, case=" + autosar1ChannelCaseId);
            Autosar1ChannelRunner.runCaseWithSummary(autosar1ChannelCaseId, 600000L);
            return;
        }
        if (autosar2ChannelCaseId != null) {
            System.out.println("Running experiment: AUTOSAR-2 Channel, case=" + autosar2ChannelCaseId);
            Autosar2ChannelRunner.runCaseWithSummary(autosar2ChannelCaseId, 600000L);
            return;
        }
        if (autosar3ChannelCaseId != null) {
            System.out.println("Running experiment: AUTOSAR-3 Channel, case=" + autosar3ChannelCaseId);
            Autosar3ChannelRunner.runCaseWithSummary(autosar3ChannelCaseId, 600000L);
            return;
        }
        if (runAllAutosarChannelBatches) {
            Autosar1ChannelBatchExecutor.main(new String[]{"--all", "--timeout-ms=600000"});
            Autosar2ChannelBatchExecutor.main(new String[]{"--all", "--timeout-ms=600000"});
            Autosar3ChannelBatchExecutor.main(new String[]{"--all", "--timeout-ms=600000"});
            return;
        }
        if (runThreeA) {
            ThreeARunner.run();
            return;
        }

        // ==================== Original AUTOSAR ====================
        // Verification experiments for AUTOSAR-1 where M_1 is a DOTA
        // import verification.experiment.autosar1.mutileM1.Experiment1_1;
        // ->       A[] (buffer1.count >=0)
        // Experiment e = new Experiment1_single_1();
        // ->       A[] (buffer1.count <= buffer1.len)
        // Experiment e = new Experiment1_single_2();
        // ->       A[] (buffer2.count >=0)
        // Experiment e = new Experiment1_single_3();
        // ->       A[] (buffer2.count <= buffer2.len)
        // Experiment e = new Experiment1_single_4();

        // Verification experiments for AUTOSAR-1 where M_1 is a composition of DOTAs
        // ->       A[] (buffer1.count >=0)
        // Experiment e = new Experiment1_1();
        // ->       A[] (buffer1.count <= buffer1.len)
        // Experiment e = new Experiment1_2();
        // ->       A[] (buffer2.count >=0)
        // Experiment e = new Experiment1_3();
        // ->       A[] (buffer2.count <= buffer2.len)
        // Experiment e = new Experiment1_4();
        // ->       A[] not (runnable1.s2 and runnable2.s2)
        // Experiment e = new Experiment1_5();
        // ->       A[] not (runnable3.s2 and runnable4.s2)
        // Experiment e = new Experiment1_6();

        // Verification experiments for AUTOSAR-2
        // ->       A[] (buffer1.count >=0)
        // Experiment e = new Experiment2_1();
        // ->       A[] (buffer1.count <= buffer1.len)
        // Experiment e = new Experiment2_2();
        // ->       A[] (buffer2.count >=0)
        // Experiment e = new Experiment2_3();
        // ->       A[] (buffer2.count <= buffer2.len)
        // Experiment e = new Experiment2_4();

        // Verification experiments for AUTOSAR-3
        // ->       A[] (buffer1.count >=0)
        // Experiment e = new Experiment3_1();
        // ->       A[] (buffer2.count >=0)
        // Experiment e = new Experiment3_2();
        // ->       A[] (buffer3.count >=0)
        // Experiment e = new Experiment3_3();

        // Verification experiments for H1-6
        // ->       A[] obs != ERROR
        // Experiment e = new H16Experiment();

        // Verification experiments for 3a3a
        // ->       A[] obs != ERROR
        // ThreeARunner.run();

        Experiment e = new H16Experiment();
        e.execute(true, false, false, 1);
    }
}
