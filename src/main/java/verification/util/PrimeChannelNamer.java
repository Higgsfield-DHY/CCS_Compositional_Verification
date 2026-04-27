package verification.util;

public final class PrimeChannelNamer {
    private PrimeChannelNamer() {
    }

    public static String internalName(String baseChannel, int primeIndex) {
        if (primeIndex <= 0) {
            return baseChannel;
        }
        return baseChannel + "_p" + primeIndex;
    }

    public static String displayName(String baseChannel, int primeIndex) {
        if (primeIndex <= 0) {
            return baseChannel;
        }
        StringBuilder builder = new StringBuilder(baseChannel);
        for (int i = 0; i < primeIndex; i++) {
            builder.append("'");
        }
        return builder.toString();
    }
}
