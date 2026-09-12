package com.urlshortener.util;

import java.math.BigInteger;
import java.security.SecureRandom;

public class AliasGenerator {
    private static final String ALIAS_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int RADIX = ALIAS_CHARS.length();
    private static final SecureRandom RNG = new SecureRandom();

    /**
     * Generates a cryptographically random, Base62-encoded alias string of the
     * specified length.
     * <p>
     * Uses a {@link java.security.SecureRandom} source to produce enough random
     * bits (approximately 6 bits per output character) and encodes the result
     * using the Base62 alphabet ({@code 0-9A-Za-z}), making it safe for use in
     * URLs.
     *
     * @param length the desired length of the generated alias, in characters;
     *               must be a positive integer
     * @return a random alphanumeric string of exactly {@code length} characters
     * @throws StringIndexOutOfBoundsException if the underlying random value
     *         encodes to a Base62 string shorter than {@code length} (rare,
     *         but possible depending on the random bytes generated)
     */
    public static String generateRandomAlias(int length) {
        int bits = (int) Math.ceil(length * 6.0);
        byte[] buf = new byte[(bits + 7) / 8];
        RNG.nextBytes(buf);
        return encodeAlisToBase62(new BigInteger(1, buf)).substring(0, length);
    }

    /**
     * Encodes a non-negative {@link BigInteger} into its Base62 string
     * representation, using the character set defined by {@code ALIAS_CHARS}
     * (typically {@code 0-9A-Za-z}, 62 total symbols).
     * <p>
     * Uses the standard repeated-division algorithm: {@code number} is
     * successively divided by the configured {@code RADIX}, and each
     * remainder (an index between 0 and {@code RADIX - 1}) is mapped to the
     * corresponding character in {@code ALIAS_CHARS}. Because remainders are
     * produced least-significant digit first, characters are appended to the
     * builder in reverse order and the final string is reversed once before
     * being returned.
     * <p>
     * The returned string has no fixed length or leading-zero padding — it is
     * exactly as long as needed to represent {@code number}. A {@code number}
     * of zero returns {@code "0"} as a special case, since the main loop
     * (which only runs while {@code number.signum() > 0}) would otherwise
     * produce an empty string.
     *
     * @param number the non-negative integer value to encode; behavior is
     *               undefined for negative values (the loop condition
     *               {@code number.signum() > 0} is {@code false} for negative
     *               numbers, so a negative input silently produces an empty
     *               string rather than throwing)
     * @return the Base62 string representation of {@code number}
     */
    private static String encodeAlisToBase62(BigInteger number) {
        if (number.signum() == 0) return "0";
        StringBuilder sb = new StringBuilder();
        BigInteger radix = BigInteger.valueOf(RADIX);
        while (number.signum() > 0) {
            BigInteger[] divRem = number.divideAndRemainder(radix);
            sb.append(ALIAS_CHARS.toCharArray()[divRem[1].intValue()]);
            number = divRem[0];
        }
        return sb.reverse().toString();
    }
}
