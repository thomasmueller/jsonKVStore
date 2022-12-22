package org.jsonKVStore.utils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A UUID implementation.
 * 
 * It supports creating version 7 UUIDs, which are time-ordered.
 * See also draft-ietf-uuidrev-rfc4122bis-00
 * 
 * Unlike java.util.UUID, the comparison is unsigned,
 * so that the string comparison yields the same result.
 */
public class Uuid implements Comparable<Uuid> {
    
    private static final AtomicLong UUID_LAST_MILLIS_AND_COUNT = new AtomicLong(0);
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    // most significant bits
    private final long msb;
    
    // least significant bits
    private final long lsb;
    
    Uuid(long msb, long lsb) {
        this.msb = msb;
        this.lsb = lsb;
    }

    @Override
    public String toString() {
        return String.format("%08x-%04x-%04x-%04x-%012x", 
                msb >>> 32, (msb >>> 16) & 0xffff, msb & 0xffff,
                (lsb >>> 48) & 0xffff, lsb & 0xffffffffffffL);
    }
    
    public String toHumanReadableString() {
        Instant instant = Instant.ofEpochMilli(getTimestampPart());
        return String.format("%s %03x %016x", 
                instant.toString(), getCounterPart(), getRandomPart());
    }
    
    public long getTimestampPart() {
        return msb >>> 16;
    }
    
    public long getCounterPart() {
        return msb & ((1L << 12) - 1);
    }
    
    public long getRandomPart() {
        return lsb;
    }

    /**
     * Unlike java.util.UUID, the comparison is unsigned,
     * so that the string comparison yields the same result.
     */
    @Override
    public int compareTo(Uuid o) {
        if (o.msb != msb) {
            return Long.compareUnsigned(msb, o.msb);
        }
        return Long.compareUnsigned(lsb, o.lsb);
    }
    
    @Override
    public int hashCode() {
        long x = lsb ^ msb;
        return (int) ((x >>> 32) ^ x);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Uuid other = (Uuid) obj;
        return lsb == other.lsb && msb == other.msb;
    }
    
    /**
     * Get the next timestamp (in milliseconds) and counter.
     * The lowest 12 bits of the returned value is the counter.
     * The milliseconds are shifted by 12 bits.
     * 
     * @param now the milliseconds
     * @param lastMillisAndCount the last returned value
     * @return the new value
     */
    static long getMillisAndCountIncreasing(long now, AtomicLong lastMillisAndCount) {
        long result = now << 12;
        while (true) {
            long last = lastMillisAndCount.get();
            if (result <= last) {
                // ensure it is non-decrementing
                result = last + 1;
            }
            long got = lastMillisAndCount.compareAndExchange(last, result);
            if (got == last) {
                return result;
            }
        }
    }

    static Uuid timeBasedVersion7(long millisAndCount,
            long random) {
        long millis = millisAndCount >>> 12;
        long counter = millisAndCount & ((1L << 12) - 1);
        long version = 7;
        long variant = 2;
        long msb = (millis << 16) | (version << 12) | counter;
        long lsb = (variant << 62) | (random & ((1L << 62) - 1));
        return new Uuid(msb, lsb);
    }

    public static Uuid timeBasedVersion7() {
        long millisAndCount = getMillisAndCountIncreasing(
                System.currentTimeMillis(), 
                UUID_LAST_MILLIS_AND_COUNT);
        long random = RANDOM.nextLong();
        return timeBasedVersion7(millisAndCount, random);
    }

    public long getMostSignificantBits() {
        return msb;
    }

    public long getLeastSignificantBits() {
        return lsb;
    }

}
