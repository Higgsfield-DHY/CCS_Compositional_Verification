package verification.uppaal.verify;

import verification.uppaal.model.NTA;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Verifyta {

    private static String command = "verifyta -S0";
    public static final long DEFAULT_TIMEOUT_MS = 20L * 60L * 1000L;
    public static final String VERIFYTA_TIMEOUT = "VERIFYTA_TIMEOUT";
    private static final String TIMEOUT_PROPERTY = "verifyta.timeout.ms";
    private static final String ISOLATED_IO_PROPERTY = "verifyta.isolated";
    private static final String COMMAND_PROPERTY = "verifyta.command";

    public static Result check(String trace, String ntaPath, String propertyPath) throws IOException {
        return check(trace, ntaPath, propertyPath, resolveTimeoutMs());
    }

    public static Result check(String trace, String ntaPath, String propertyPath, long timeoutMs) throws IOException {
        Process process = new ProcessBuilder(buildCommandTokens(trace, ntaPath, propertyPath)).start();
        StringBuffer buffer = new StringBuffer();

        Thread stdoutThread = readStreamAsync(process.getInputStream(), buffer);
        Thread stderrThread = readStreamAsync(process.getErrorStream(), buffer);

        boolean finished;
        try {
            finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new IOException("verifyta interrupted", e);
        }
        if (!finished) {
            process.destroyForcibly();
            throw new IOException(VERIFYTA_TIMEOUT + ": process exceeded " + timeoutMs + " ms");
        }

        joinQuietly(stdoutThread);
        joinQuietly(stderrThread);
        return new Result(buffer.toString());
    }

    private static long resolveTimeoutMs() {
        String property = System.getProperty(TIMEOUT_PROPERTY);
        if (property == null || property.trim().isEmpty()) {
            return DEFAULT_TIMEOUT_MS;
        }
        try {
            long parsed = Long.parseLong(property.trim());
            if (parsed > 0) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
            // fall through to default
        }
        return DEFAULT_TIMEOUT_MS;
    }

    public static boolean isIsolatedWorkspaceEnabled() {
        String property = System.getProperty(ISOLATED_IO_PROPERTY);
        if (property == null || property.trim().isEmpty()) {
            return true;
        }
        return !"false".equalsIgnoreCase(property.trim());
    }

    public static String getWorkRootPath() {
        return VerifytaWorkspace.resolveRootPath().toString();
    }

    public static String getCommand() {
        String override = System.getProperty(COMMAND_PROPERTY);
        if (override != null && !override.trim().isEmpty()) {
            return override.trim();
        }
        return command;
    }

    private static List<String> buildCommandTokens(String trace, String ntaPath, String propertyPath) {
        List<String> commandList = new ArrayList<>();
        String resolvedCommand = getCommand();
        String commandText = resolvedCommand == null ? "" : resolvedCommand.trim();
        if (!commandText.isEmpty()) {
            for (String token : commandText.split("\\s+")) {
                if (!token.isEmpty()) {
                    commandList.add(token);
                }
            }
        }
        String traceText = trace == null ? "" : trace.trim();
        if (!traceText.isEmpty()) {
            for (String token : traceText.split("\\s+")) {
                if (!token.isEmpty()) {
                    commandList.add(token);
                }
            }
        }
        commandList.add(ntaPath);
        commandList.add(propertyPath);
        return commandList;
    }

    private static Thread readStreamAsync(final InputStream stream, final StringBuffer output) {
        Thread thread = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException ignored) {
                // ignore stream close race on process termination
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static void joinQuietly(Thread thread) {
        try {
            thread.join(1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Verify whether the NTA satisfies a given property.
     */
    public static Result isSatisfied(NTA nta, String statement) throws IOException {
        String trace = "-t1";
        if (!isIsolatedWorkspaceEnabled()) {
            String base = ".\\src\\main\\resources\\verification\\";
            String ntaPath = base + nta.getName() + ".xml";
            nta.writeToUppaalXml(ntaPath);
            String statementPath = base + "nta.q";
            writeProperty(statementPath, statement);
            return check(trace, ntaPath, statementPath);
        }

        Path runDir = VerifytaWorkspace.createRunDirectory();
        Path ntaPath = runDir.resolve(nta.getName() + ".xml");
        Path statementPath = runDir.resolve("nta.q");
        nta.writeToUppaalXml(ntaPath.toString());
        writeProperty(statementPath.toString(), statement);
        return check(trace, ntaPath.toString(), statementPath.toString());
    }

    public static void writeProperty(String path, String state) {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(path), StandardCharsets.UTF_8)) {
            writer.write(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        String ntaPath = ".\\src\\main\\resources\\verification\\nta.xml";
        String propertyPath = ".\\src\\main\\resources\\verification\\nta.q";
        String trace = "-t1";
        Result result = Verifyta.check(trace, ntaPath, propertyPath);
        System.out.println(result);
    }
}
