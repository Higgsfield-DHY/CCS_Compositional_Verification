package verification.experiment.channel;

import verification.experiment.Experiment;

public final class ChannelExperimentSupport {
    public static final long DEFAULT_TIMEOUT_MS = 10L * 60L * 1000L;
    public static final String TIMEOUT_ARG_PREFIX = "--timeout-ms=";
    private static final String VERIFYTA_TIMEOUT_PROPERTY = "verifyta.timeout.ms";

    private ChannelExperimentSupport() {
    }

    public static long parseTimeoutMs(String[] args) {
        if (args == null) {
            return DEFAULT_TIMEOUT_MS;
        }
        for (String arg : args) {
            if (arg != null && arg.startsWith(TIMEOUT_ARG_PREFIX)) {
                String raw = arg.substring(TIMEOUT_ARG_PREFIX.length()).trim();
                if (raw.isEmpty()) {
                    break;
                }
                try {
                    long parsed = Long.parseLong(raw);
                    if (parsed > 0L) {
                        return parsed;
                    }
                } catch (NumberFormatException ignored) {
                    break;
                }
            }
        }
        return DEFAULT_TIMEOUT_MS;
    }

    public static <T> T withVerifytaTimeout(long timeoutMs, CheckedSupplier<T> action) throws Exception {
        String old = System.getProperty(VERIFYTA_TIMEOUT_PROPERTY);
        if (timeoutMs > 0L) {
            System.setProperty(VERIFYTA_TIMEOUT_PROPERTY, String.valueOf(timeoutMs));
        }
        try {
            return action.get();
        } finally {
            if (old == null) {
                System.clearProperty(VERIFYTA_TIMEOUT_PROPERTY);
            } else {
                System.setProperty(VERIFYTA_TIMEOUT_PROPERTY, old);
            }
        }
    }

    public static Experiment instantiateSourceExperiment(String sourceCase) {
        if (sourceCase == null || sourceCase.trim().isEmpty()) {
            throw new IllegalArgumentException("sourceCase must not be empty.");
        }
        String fqcn = sourceCase.startsWith("verification.experiment.")
                ? sourceCase.trim()
                : "verification.experiment." + sourceCase.trim();
        try {
            Class<?> clazz = Class.forName(fqcn);
            Object instance = clazz.newInstance();
            if (!(instance instanceof Experiment)) {
                throw new IllegalStateException("Source case is not an Experiment: " + fqcn);
            }
            return (Experiment) instance;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate source experiment: " + fqcn, e);
        }
    }

    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}
