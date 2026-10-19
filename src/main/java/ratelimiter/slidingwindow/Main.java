package ratelimiter.slidingwindow;

import ratelimiter.fixedwindow.FixedWindowRateLimiter;
import ratelimiter.common.InMemoryRateLimitConfigProvider;
import ratelimiter.common.RateLimitConfig;
import ratelimiter.common.RateLimiter;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Deterministic comparison: requests expire by age, even across a fixed-window boundary. */
public class Main {
    public static void main(String[] args) {
        var clock = new DemoClock();
        var provider = new InMemoryRateLimitConfigProvider(new RateLimitConfig(2, 1_000));
        RateLimiter fixed = new FixedWindowRateLimiter(provider, clock);
        RateLimiter sliding = new SlidingWindowRateLimiter(provider, clock);
        clock.millis = 999;
        for (int i = 1; i <= 2; i++) {
            System.out.println("t=999, request " + i + ": fixed=" + fixed.allowRequest("alice")
                    + ", sliding=" + sliding.allowRequest("alice"));
        }
        clock.millis = 1_000;
        System.out.println("t=1000: fixed=" + fixed.allowRequest("alice")
                + ", sliding=" + sliding.allowRequest("alice"));
        clock.millis = 1_999;
        System.out.println("t=1999, old requests expire: sliding=" + sliding.allowRequest("alice"));
        provider.configure("alice", new RateLimitConfig(1, 1_000));
        System.out.println("After config update: " + sliding.allowRequest("alice"));
        System.out.println("Quota exhausted: " + sliding.allowRequest("alice"));
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
