package ratelimiter.tokenbucket;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Deterministic walkthrough: no sleeps or background refill thread. */
public final class Main {
    public static void main(String[] args) {
        var clock = new DemoClock();
        var limiter = new TokenBucketRateLimiter(3, clock);
        print(limiter, clock, "alice", 4);
        print(limiter, clock, "bob", 1);
        clock.millis = 4_999;
        print(limiter, clock, "alice", 1);
        clock.millis = 5_000;
        print(limiter, clock, "alice", 3);
        clock.millis = 100_000;
        print(limiter, clock, "alice", 4);
    }

    private static void print(TokenBucketRateLimiter limiter, DemoClock clock,
                              String user, int count) {
        for (int i = 0; i < count; i++) {
            System.out.printf("t=%dms user=%s accepted=%s%n",
                    clock.millis, user, limiter.allowRequest(user));
        }
    }

    private static final class DemoClock extends Clock {
        private long millis;
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) {
            if (!ZoneOffset.UTC.equals(zone)) throw new UnsupportedOperationException();
            return this;
        }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis); }
        @Override public long millis() { return millis; }
    }
}
