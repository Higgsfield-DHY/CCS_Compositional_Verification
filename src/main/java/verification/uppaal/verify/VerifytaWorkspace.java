package verification.uppaal.verify;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

final class VerifytaWorkspace {
    private static final String WORKDIR_PROPERTY = "verifyta.workdir";

    private VerifytaWorkspace() {
    }

    static Path resolveRootPath() {
        String configured = System.getProperty(WORKDIR_PROPERTY);
        if (configured != null && !configured.trim().isEmpty()) {
            return Paths.get(configured.trim());
        }
        return Paths.get(System.getProperty("java.io.tmpdir"), "verifyta-runs");
    }

    static Path createRunDirectory() throws IOException {
        Path root = resolveRootPath();
        Files.createDirectories(root);

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.ROOT).format(new Date());
        String pid = extractPid();
        long threadId = Thread.currentThread().getId();
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);

        Path runDir = root.resolve(timestamp + "-p" + pid + "-t" + threadId + "-" + randomSuffix);
        Files.createDirectories(runDir);
        return runDir;
    }

    private static String extractPid() {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        int idx = jvmName.indexOf('@');
        if (idx > 0) {
            return jvmName.substring(0, idx);
        }
        return "unknown";
    }
}
