package org.jsonKVStore.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;

public class Converter {
    
    private static final Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Decoder BASE64_DECODER = Base64.getDecoder();
    
    public static String combine(List<String> elements) {
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                buff.append('/');
            }
            buff.append(elements.get(i));
        }
        return buff.toString();
    }
    
    public static List<String> split(String s) {
        ArrayList<String> result = new ArrayList<>();
        while (true) {
            int end;
            if (s.startsWith("\'")) {
                end = findQuoteEnd(s);
                String x = s.substring(0, end);
                result.add(x);
                if (end == s.length()) {
                    break;
                }
            } else {
                end = s.indexOf('/');
                if (end < 0) {
                    result.add(s);
                    break;
                } else {
                    result.add(s.substring(0, end));
                }
            }
            if (s.charAt(end) != '/') {
                throw new IllegalArgumentException("Expected '/', got " + s);
            }
            s = s.substring(end + 1);
        }
        return result;
    }
    
    public static String unsignedLongToString(long x) {
        String s = Long.toHexString(x);
        s = Integer.toHexString(s.length() - 1) + s;
        return s;
    }
    
    public static long stringToUnsignedLong(String s) {
        return Long.parseUnsignedLong(s.substring(1), 16);
    }
    
    public static String quote(String s) {
        return "'" + s.replaceAll("'", "''") + "'";
    }
    
    public static String unquote(String s) {
        if (!s.startsWith("'") || !s.endsWith("'") || s.length() < 2) {
            throw new IllegalArgumentException("Not wrapped in '': " + s);
        }
        s = s.substring(1, s.length() - 1); 
        return s.replaceAll("''", "'");
    }
    
    public static int findQuoteEnd(String s) {
        if (!s.startsWith("'")) {
            throw new IllegalArgumentException("Doesn't start with ': " + s);
        }
        int start = 1;
        while (true) {
            int index = s.indexOf('\'', start);
            if (index < 0) {
                throw new IllegalArgumentException("Doesn't end with ': " + s);
            }
            if (index >= s.length() - 1) {
                return s.length();
            }
            if (s.charAt(index + 1) != '\'') {
                return index + 1;
            }
            start = index + 2;
        }
    }
    
    
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
    
    public static String toBase64(byte[] data) {
        return BASE64_ENCODER.encodeToString(data);
    }

    public static void fromBase64(String s, byte[] target) {
        BASE64_DECODER.decode(s.getBytes(), target);
    }

}
