package ratelimiter.slidingwindowcounter;

import ratelimiter.common.RateLimiter;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Follow the basic two-counter example first, then the 13-slot circular array. */
public class Main {
    public static void main(String[] args) {
        demonstrateTwoCounters();
        demonstrateFiveSecondBuckets();
    }

    private static void demonstrateTwoCounters() {
        System.out.println("Two counters: 4 requests / 60 seconds");
        var clock = new DemoClock();
        RateLimiter limiter = new SlidingWindowCounterRateLimiter(4, 60_000, clock);
        clock.millis = 59_999;
        printRequests(limiter, "t=59999", 4);
        clock.millis = 60_000;
        printRequests(limiter, "t=60000, previous contributes 4", 1);
        clock.millis = 90_000;
        printRequests(limiter, "t=90000, previous contributes 2", 3);
        // At t=90000 all original requests are still within 60s: these extra admissions
        // demonstrate approximation error, not an exact rolling-quota guarantee.
    }

    private static void demonstrateFiveSecondBuckets() {
        System.out.println("5-second buckets: 4 requests / 60 seconds, 13 slots");
        var clock = new DemoClock();
        RateLimiter limiter = new BucketedSlidingWindowCounterRateLimiter(4, 60_000, clock);
        clock.millis = 4_999;
        printRequests(limiter, "t=4999, bucket 0", 3);
        clock.millis = 60_000;
        printRequests(limiter, "t=60000, retain bucket 0 and active bucket 12", 2);
        clock.millis = 65_000;
        printRequests(limiter, "t=65000, slot 0 reused for bucket 13", 4);
    }

    private static void printRequests(RateLimiter limiter, String label, int requests) {
        for (int i = 1; i <= requests; i++) {
            System.out.println(label + ", request " + i + ": " + limiter.allowRequest("alice"));
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
