package verification.experiment.validation;

import verification.experiment.Experiment;
import verification.experiment.channel.DirectSystemVerifier;
import verification.plugins.SequenceChecker;
import verification.report.AgRunReport;
import verification.report.AgVerdict;
import verification.uppaal.model.Declaration;
import verification.uppaal.model.Template;
import verification.uppaal.verify.Verifyta;
import verification.util.ChannelPreprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

final class ValidationSupport {
    static final long DEFAULT_TIMEOUT_MS = Verifyta.DEFAULT_TIMEOUT_MS;
    static final long KNOWN_LONG_TIMEOUT_MS = 30L * 60L * 1000L;

    private ValidationSupport() {
    }

    interface ExperimentFactory {
        Experiment create() throws Exception;
    }

    enum RunStatus {
        PASS,
        FAIL,
        ERROR,
        SKIPPED,
        KNOWN_LONG
    }

    static class CaseOutcome {
        String caseId;
        String group;
        String verdict = "UNKNOWN";
        String directTruth = "UNKNOWN";
        RunStatus status = RunStatus.ERROR;
        int cq1 = -1;
        int cq2 = -1;
        int states = -1;
        long elapsedMs = -1L;
        String note = "-";
        AgRunReport report;
    }

    static void printRuntimeContext(String runnerName) {
        System.out.println("============================================================");
        System.out.println("运行器: " + runnerName);
        System.out.println("JDK版本: " + System.getProperty("java.version"));
        System.out.println("JDK路径: " + System.getProperty("java.home"));
        System.out.println("verifyta命令: " + Verifyta.getCommand());
        System.out.println("隔离工作目录: " + Verifyta.isIsolatedWorkspaceEnabled());
        System.out.println("工作目录根: " + Verifyta.getWorkRootPath());
        System.out.println("默认超时(ms): " + Verifyta.DEFAULT_TIMEOUT_MS);
        System.out.println("============================================================");
    }

    static void cleanupStaleVerifytaProcesses() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!osName.contains("win")) {
            return;
        }
        try {
            Process process = new ProcessBuilder("cmd", "/c", "taskkill /F /IM verifyta.exe >nul 2>&1").start();
            process.waitFor(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // best-effort cleanup only
        }
    }

    static CaseOutcome runExperimentCase(String caseId,
                                         String group,
                                         ExperimentFactory factory,
                                         long timeoutMs) {
        CaseOutcome outcome = new CaseOutcome();
        outcome.caseId = caseId;
        outcome.group = group;

        String oldTimeout = System.getProperty("verifyta.timeout.ms");
        if (timeoutMs > 0) {
            System.setProperty("verifyta.timeout.ms", String.valueOf(timeoutMs));
        }

        long begin = System.currentTimeMillis();
        try {
            Experiment directExp = factory.create();
            AgVerdict directTruth = runQuietly(new ThrowingSupplier<AgVerdict>() {
                @Override
                public AgVerdict get() throws Exception {
                    return runDirectTruth(directExp);
                }
            });
            outcome.directTruth = directTruth.name();

            Experiment agExp = factory.create();
            boolean sequenceCheck = hasSequenceChecker(agExp.getSequenceChecker());
            AgRunReport report = runQuietly(new ThrowingSupplier<AgRunReport>() {
                @Override
                public AgRunReport get() throws Exception {
                    return agExp.executeWithReport(true, false, sequenceCheck, 1);
                }
            });
            outcome.report = report;
            if (report != null) {
                outcome.verdict = report.getVerdict().name();
                outcome.cq1 = report.getCq1FailCount();
                outcome.cq2 = report.getCq2FailCount();
                outcome.states = report.getFinalStateCount();
            }
            if (report == null) {
                outcome.status = RunStatus.ERROR;
                outcome.note = "AG report is null";
            } else if (!outcome.directTruth.equals(outcome.verdict)) {
                outcome.status = RunStatus.FAIL;
                outcome.note = "DirectTruth=" + outcome.directTruth + ", AGVerdict=" + outcome.verdict;
            } else {
                outcome.status = RunStatus.PASS;
                outcome.note = "OK";
            }
        } catch (Exception e) {
            outcome.status = RunStatus.ERROR;
            outcome.note = e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage());
        } finally {
            if (oldTimeout == null) {
                System.clearProperty("verifyta.timeout.ms");
            } else {
                System.setProperty("verifyta.timeout.ms", oldTimeout);
            }
            outcome.elapsedMs = System.currentTimeMillis() - begin;
        }

        return outcome;
    }

    static CaseOutcome runH16BatchCase(long timeoutMs) {
        CaseOutcome outcome = new CaseOutcome();
        outcome.caseId = "H16_BATCH_8";
        outcome.group = "H16";
        outcome.verdict = "BATCH";

        List<String> cmd = new ArrayList<>();
        cmd.add(resolveJavaExecutable());
        cmd.add("-Dfile.encoding=UTF-8");
        cmd.add("-Dverifyta.timeout.ms=" + timeoutMs);
        cmd.add("-cp");
        cmd.add(System.getProperty("java.class.path"));
        cmd.add("verification.experiment.h16.H16BatchExecutor");

        long begin = System.currentTimeMillis();
        StringBuilder output = new StringBuilder();
        try {
            Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            Thread reader = new Thread(() -> readProcessOutput(process, output));
            reader.setDaemon(true);
            reader.start();

            boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                outcome.status = RunStatus.ERROR;
                outcome.note = Verifyta.VERIFYTA_TIMEOUT + ": H16 batch exceeded " + timeoutMs + " ms";
            } else if (process.exitValue() == 0) {
                outcome.status = RunStatus.PASS;
                outcome.note = "OK";
            } else {
                outcome.status = RunStatus.FAIL;
                outcome.note = clip(output.toString(), 300);
            }
            reader.join(2000L);
        } catch (Exception e) {
            outcome.status = RunStatus.ERROR;
            outcome.note = e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage());
        } finally {
            outcome.elapsedMs = System.currentTimeMillis() - begin;
        }
        return outcome;
    }

    static CaseOutcome skipped(String caseId, String group, String note) {
        CaseOutcome outcome = new CaseOutcome();
        outcome.caseId = caseId;
        outcome.group = group;
        outcome.status = RunStatus.SKIPPED;
        outcome.note = note;
        return outcome;
    }

    static boolean isVerifytaTimeout(CaseOutcome outcome) {
        return outcome != null
                && outcome.note != null
                && outcome.note.contains(Verifyta.VERIFYTA_TIMEOUT);
    }

    private static String resolveJavaExecutable() {
        String javaHome = System.getProperty("java.home");
        String sep = System.getProperty("file.separator");
        return javaHome + sep + "bin" + sep + "java";
    }

    private static void readProcessOutput(Process process, StringBuilder output) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line).append('\n');
            }
        } catch (IOException ignored) {
            // ignore stream close race
        }
    }

    private static String clip(String text, int maxLen) {
        if (text == null || text.isEmpty()) {
            return "-";
        }
        if (text.length() <= maxLen) {
            return text.replace('\n', ' ');
        }
        return text.substring(0, maxLen).replace('\n', ' ') + "...";
    }

    private static AgVerdict runDirectTruth(Experiment experiment) throws IOException {
        Declaration globalDeclaration = experiment.getGlobalDeclaration();
        List<Template> m1 = experiment.getM1();
        List<Template> m2 = experiment.getM2();
        if (experiment.isPortActionMode()) {
            ChannelPreprocessor.preprocessPortMode(
                    globalDeclaration, m1, m2, experiment.getM1RenameMap(), experiment.getM2RenameMap(),
                    experiment.getTargetSigma(), experiment.getPortPreprocessConfig());
        }
        return DirectSystemVerifier.verify(globalDeclaration, m1, m2, experiment.getStatement());
    }

    private static boolean hasSequenceChecker(List<SequenceChecker> sequenceCheckers) {
        return sequenceCheckers != null && !sequenceCheckers.isEmpty();
    }

    private static <T> T runQuietly(ThrowingSupplier<T> action) throws Exception {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        PrintStream quiet = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        });
        try {
            System.setOut(quiet);
            System.setErr(quiet);
            return action.get();
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
            quiet.close();
        }
    }

    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
