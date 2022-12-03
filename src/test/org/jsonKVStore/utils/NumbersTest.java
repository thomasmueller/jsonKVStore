package org.jsonKVStore.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

public class NumbersTest {

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
        for(long x = 0; x > 0; x = (long) ((x * 1.1) + 1)) {
            String s = testUnsignedLongToString(x);
            assertTrue(s.compareTo(last) > 0);
            last = s;
        }
    }
    
    private String testUnsignedLongToString(long x) {
        String result = Numbers.unsignedLongToString(x);
        assertEquals(x, Numbers.stringToUnsignedLong(result));
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
        String result = Numbers.decimalToString(bd);
        BigDecimal test = Numbers.stringToDecimal(result);
        assertTrue(bd.toString() + " " + test.toString(), bd.compareTo(test) == 0);
        return result;
    }
    
//    @Test
//    public void sortableDouble() {
////        assertEquals("0000", testDoubleToString(0));
////        assertEquals("0000", testDoubleToString(1));
//        assertEquals("03c4000000000000", testDoubleToString(0xa));
//        assertEquals("03ce000000000000", testDoubleToString(0xf));
//        assertEquals("0400", testDoubleToString(0x10));
//        assertEquals("09c1100000000000", testDoubleToString(0x222));
//        assertEquals("13f00", testDoubleToString(Long.MAX_VALUE));
//        assertEquals("-13fcfffffffffffff", testDoubleToString(Long.MIN_VALUE));
//        assertEquals("-00cfffffffffffff", testDoubleToString(-1));
//        String last = "";
//        for(double x = 0; x < Double.POSITIVE_INFINITY; x = ((x * 1.1) + 1)) {
//            String s = testDoubleToString(x);
//            assertTrue(s.compareTo(last) > 0);
//            last = s;
//        }
//    }
//    
//    private String testDoubleToString(double x) {
//        String result = Numbers.doubleToString(x);
//        double y = Numbers.stringToDouble(result);
//        assertEquals(Double.doubleToRawLongBits(x), Double.doubleToRawLongBits(y));
//        return result;
//    }
}
