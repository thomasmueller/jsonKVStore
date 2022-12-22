package org.jsonKVStore.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;

public class ConverterTest {

    @Test(expected=IllegalArgumentException.class)
    public void unquote() {
        Converter.unquote("'");
    }
    
    @Test
    public void combineAndSplit() {
        assertEquals("0/1/2", testCombine(List.of("0", "1", "2")));
        assertEquals("'hello'/'joe''s'/3", testCombine(List.of("'hello'", "'joe''s'", "3")));
        assertEquals("'joe''s'", testCombine(List.of("'joe''s'")));
    }
    
    private static String testCombine(List<String> list) {
        String result = Converter.combine(list);
        List<String> split = Converter.split(result);
        assertEquals(list.toString(), split.toString());
        return result;
    }
    
    
    @Test
    public void quote() {
        assertEquals("''", testQuote(""));
        assertEquals("''''", testQuote("'"));
        assertEquals("'test'", testQuote("test"));
    }
    
    private static String testQuote(String s) {
        String result = Converter.quote(s);
        assertEquals(s, Converter.unquote(result));
        return result;
    }

    @Test
    public void sortableLong() {
        assertEquals("00", testUnsignedLongToString(0));
        assertEquals("01", testUnsignedLongToString(1));
        assertEquals("0a", testUnsignedLongToString(0xa));
        assertEquals("0f", testUnsignedLongToString(0xf));
        assertEquals("110", testUnsignedLongToString(0x10));
        assertEquals("2222", testUnsignedLongToString(0x222));
        assertEquals("f7fffffffffffffff", testUnsignedLongToString(Long.MAX_VALUE));
        assertEquals("f8000000000000000", testUnsignedLongToString(Long.MIN_VALUE));
        assertEquals("fffffffffffffffff", testUnsignedLongToString(-1));
        String last = "";
        for(long x = 1; x > 0; x = (long) ((x * 11 / 10) + 1)) {
            String s = testUnsignedLongToString(x);
            assertTrue(s.compareTo(last) > 0);
            last = s;
        }
    }
    
    private String testUnsignedLongToString(long x) {
        String result = Converter.unsignedLongToString(x);
        assertEquals(x, Converter.stringToUnsignedLong(result));
        return result;
    }
    
    @Test
    public void sortableBigDecimal() {
        assertEquals("168810776627963145224", testBigDecimalToString(Long.MIN_VALUE));
        assertEquals("179", testBigDecimalToString(-1));
        assertEquals("18015", testBigDecimalToString(-0.5));
        assertEquals("2", testBigDecimalToString(0));
        assertEquals("31985", testBigDecimalToString(0.5));
        assertEquals("321", testBigDecimalToString(1));
        assertEquals("33011", testBigDecimalToString(0xa));
        assertEquals("330115", testBigDecimalToString(0xf));
        assertEquals("330116", testBigDecimalToString(0x10));
        assertEquals("3302546", testBigDecimalToString(0x222));
        assertEquals("331189223372036854776", testBigDecimalToString(Long.MAX_VALUE));
        String last = "";
        for(double x = 0; x < Double.POSITIVE_INFINITY; x = ((x * 1.1) + 1)) {
            String s = testBigDecimalToString(x);
            assertTrue(s.compareTo(last) > 0);
            last = s;
        }
    }
    
    private String testBigDecimalToString(double x) {
        BigDecimal bd = BigDecimal.valueOf(x);
        String result = Converter.decimalToString(bd);
        BigDecimal test = Converter.stringToDecimal(result);
        assertTrue(bd.toString() + " " + test.toString(), bd.compareTo(test) == 0);
        return result;
    }
    
}
