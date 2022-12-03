package org.jsonKVStore.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Numbers {
    
    public static String unsignedLongToString(long x) {
        String s = Long.toHexString(x);
        s = Integer.toHexString(s.length() - 1) + s;
        return s;
    }
    
    public static long stringToUnsignedLong(String s) {
        return Long.parseUnsignedLong(s.substring(1), 16);
    }
    
//
//    public static String doubleToString(double x) {
//        long ieee754 = Double.doubleToRawLongBits(x);
//        long sign = ieee754 >>> 63;
//        ieee754 ^= sign << 63;
//        long exponent = (ieee754 >> 52);
//        ieee754 ^= exponent << 52;
//        exponent -= 1023;
//        long fraction = ieee754;
//        if (sign != 0) {
////            exponent ^= ((1L << 12) - 1);
//            fraction ^= ((1L << 52) - 1);
//        }
//        String result = unsignedLongToString(exponent) + unsignedLongToString(fraction);
//        return sign != 0 ? "-" + result : result;
//    }
//    
//    public static double stringToDouble(String s) {
//        int sign = 0;
//        if (s.startsWith("-")) {
//            s = s.substring(1);
//            sign = 1;
//        }
//        int expLength = 1 + Integer.parseInt(s.substring(0, 1), 16);
//        long exponent = Long.parseUnsignedLong(s.substring(1, 1 + expLength), 16);
//        int fracLength = 1 + Integer.parseInt(s.substring(1 + expLength, 2 + expLength), 16);
//        long fraction = Long.parseUnsignedLong(s.substring(2 + expLength, 2 + expLength + fracLength), 16);
//        if (sign != 0) {
////            exponent ^= ((1L << 12) - 1);
//            fraction ^= ((1L << 52) - 1);
//        }
//        exponent += 1023;
//        long ieee754 = ((long) sign << 63) | (exponent << 52) | fraction;
//        return Double.longBitsToDouble(ieee754);
//    }

    // see https://svn.apache.org/repos/asf/jackrabbit/trunk/jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/DecimalField.java
    
    /**
     * Convert a BigDecimal to a String.
     * 
     * @param value the BigDecimal
     * @return the String
     */
    public static String decimalToString(BigDecimal value) {
        switch (value.signum()) {
        case -1:
            return "1" + invert(positiveDecimalToString(value.negate()), 1);
        case 0:
            return "2";
        default:
            return "3" + positiveDecimalToString(value);
        }
    }
    
    /**
     * Convert a String to a BigDecimal.
     * 
     * @param value the String
     * @return the BigDecimal
     */
    public static BigDecimal stringToDecimal(String value) {
        int sig = value.charAt(0) - '2';
        if (sig == 0) {
            return BigDecimal.ZERO;
        } else if (sig < 0) {
            value = invert(value, 1);
        }
        long expSig = value.charAt(1) - '2', exp;
        if (expSig == 0) {
            exp = 0;
            value = value.substring(2);
        } else {
            int expSize = value.charAt(2) - '0' + 1;
            if (expSig < 0) {
                expSize = 11 - expSize;
            }
            String e = value.substring(3, 3 + expSize);
            exp = expSig * Long.parseLong(expSig < 0 ? invert(e, 0) : e);
            value = value.substring(3 + expSize);
        }
        BigInteger x = new BigInteger(value);
        int scale = (int) (value.length() - exp - 1);
        return new BigDecimal(sig < 0 ? x.negate() : x, scale);
    }
    
    private static String positiveDecimalToString(BigDecimal value) {
        StringBuilder buff = new StringBuilder();
        long exp = value.precision() - value.scale() - 1;
        // exponent signum and size
        if (exp == 0) {
            buff.append('2');
        } else {
            String e = String.valueOf(Math.abs(exp));
            // exponent size is prepended
            e = String.valueOf(e.length() - 1) + e;
            // exponent signum
            if (exp > 0) {
                buff.append('3').append(e);
            } else {
                buff.append('1').append(invert(e, 0));
            }
        }
        String s = value.unscaledValue().toString();
        // remove trailing 0s
        int max = s.length() - 1;
        while (s.charAt(max) == '0') {
            max--;
        }
        return buff.append(s.substring(0, max + 1)).toString();
    }

    /**
     * "Invert" a number digit by digit (0 becomes 9, 9 becomes 0, and so on).
     * 
     * @param s the original string
     * @param incLast how much to increment the last character
     * @return the negated string
     */
    private static String invert(String s, int incLast) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ('9' - chars[i] + '0');
        }
        chars[chars.length - 1] += incLast;
        return String.valueOf(chars);
    }
    
}
