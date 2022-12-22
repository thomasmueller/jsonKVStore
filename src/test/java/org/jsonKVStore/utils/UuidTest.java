package org.jsonKVStore.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class UuidTest {

    @Test
    public void convert() {
        Random r = new Random(1);
        for (int i = 0; i < 1000; i++) {
            long msb = r.nextLong();
            long lsb = r.nextLong();
            UUID a = new UUID(msb, lsb);
            Uuid b = new Uuid(msb, lsb);
            assertEquals(a.toString(), b.toString());
        }
    }

    @Test
    public void compare() {
        Random r = new Random(1);
        Uuid lastY = new Uuid(0, 0);
        for (int i = 0; i < 1000; i++) {
            long a = r.nextBoolean() ? r.nextInt(10) : r.nextLong();
            long b = r.nextBoolean() ? r.nextInt(10) : r.nextLong();
            Uuid y = new Uuid(a, b);
            assertEquals((int) Math.signum(y.compareTo(lastY)), 
                    (int) Math.signum(y.toString().compareTo(lastY.toString())));
            if (y.compareTo(lastY) == 0) {
                assertEquals(y.hashCode(), lastY.hashCode());
                assertTrue(y.equals(lastY));
            } else {
                assertFalse(y.equals(lastY));
            }
            lastY = y;
        }
    }
    
    @Test
    public void versionAndVariant() {
        Uuid x = Uuid.timeBasedVersion7();
        UUID y = new UUID(x.getMostSignificantBits(), x.getLeastSignificantBits());
        assertEquals(7, y.version());
        assertEquals(2, y.variant());
    }

    @Test
    public void incremental() {
        Uuid last = Uuid.timeBasedVersion7();
        for (int i = 0; i < 1000; i++) {
            Uuid x = Uuid.timeBasedVersion7();
            assertTrue(x.compareTo(last) >= 0);
            last = x;
        }
    }
    
    @Test
    public void getMillisIncreasing() {
        AtomicLong lastMillis = new AtomicLong();
        assertEquals((10 << 12) + 0, Uuid.getMillisAndCountIncreasing(10, lastMillis));
        assertEquals((10 << 12) + 0, lastMillis.get());
        assertEquals((10 << 12) + 1, Uuid.getMillisAndCountIncreasing(9, lastMillis));
        assertEquals((10 << 12) + 1, lastMillis.get());
        assertEquals((11 << 12) + 0, Uuid.getMillisAndCountIncreasing(11, lastMillis));
        assertEquals((11 << 12) + 0, lastMillis.get());
    }
    
    @Test
    public void fastGeneration() {
        int size = 1 << 14;
        ArrayList<Uuid> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(Uuid.timeBasedVersion7());
        }
        for (int i = 1; i < size; i++) {
            Uuid a = list.get(i - 1);
            Uuid b = list.get(i);
            assertFalse(a.equals(b));
            assertTrue(a.compareTo(b) < 0);
        }
    }    

}
