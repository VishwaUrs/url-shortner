package com.urlshortener.util;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;

public class AliasGenerator {
    private static final String ALIAS_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int RADIX = ALIAS_CHARS.length();
    private static final SecureRandom RNG = new SecureRandom();

    /**
     * Generates a random number and passes it on to encode method to get its Base62 version
     * @param  length: Length of the random number to be generated
     * @return String : Base62 equivalent of the Random 7 Digit number
     */
    public static String randomAliasGenerate(int length) {
        int bits = (int) Math.ceil(length * 6.0);
        byte[] buf = new byte[(bits + 7) / 8];
        RNG.nextBytes(buf);
        return encodeAlisToBase62(new BigInteger(1, buf)).substring(0, length);
    }

    private static String encodeAlisToBase62(BigInteger number) {
        System.out.println(number);
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
