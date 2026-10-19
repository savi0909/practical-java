package ratelimiter.slidingwindowcounter;

import ratelimiter.common.RateLimiter;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

final class CounterTestSupport {
    private CounterTestSupport() {}

    static void exhaust(RateLimiter limiter, String user, int quota) {
        for (int i = 0; i < quota; i++) assertTrue(limiter.allowRequest(user));
        assertFalse(limiter.allowRequest(user));
        assertFalse(limiter.allowRequest(user));
    }

    static final class MutableClock extends Clock {
        volatile long millis;
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) {
            if (!ZoneOffset.UTC.equals(zone)) throw new UnsupportedOperationException();
            return this;
        }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis); }
        @Override public long millis() { return millis; }
    }
}
