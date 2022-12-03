package org.jsonKVStore;

import org.junit.Test;

public class UtilsTest {
    
    @Test
    public void test() {
        for(long i=0; i<Integer.MAX_VALUE; i++) {
            long pow2 = maxPower2(i);
            if (i < 10) {
                System.out.println(i + ": " + pow2);
            } else {
                break;
            }
        }
    }

    private long maxPower2(long x) {
        return Long.highestOneBit(x);
    }
}
