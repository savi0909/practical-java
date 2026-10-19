package ratelimiter.slidingwindowcounter;

import org.junit.jupiter.api.Test;
import ratelimiter.common.InMemoryRateLimitConfigProvider;
import ratelimiter.common.RateLimitConfig;

import static org.junit.jupiter.api.Assertions.*;
import static ratelimiter.slidingwindowcounter.CounterTestSupport.*;

class SlidingWindowCounterRateLimiterTest {
    private final MutableClock clock = new MutableClock();
    private final InMemoryRateLimitConfigProvider provider =
            new InMemoryRateLimitConfigProvider(new RateLimitConfig(4, 60_000));
    private final SlidingWindowCounterRateLimiter limiter =
            new SlidingWindowCounterRateLimiter(provider, clock);

    @Test
    void admitsQuotaAndRejectsRepeatedCallsAtTheSameTime() {
        exhaust(limiter, "alice", 4);
        exhaust(limiter, "bob", 4);
    }

    @Test
    void carriesCurrentIntoPreviousAtExactBoundaryAndWeightsItHalfwayThrough() {
        clock.millis = 59_999;
        exhaust(limiter, "alice", 4);
        clock.millis = 60_000;
        assertFalse(limiter.allowRequest("alice")); // previous still contributes 4
        clock.millis = 90_000;
        exhaust(limiter, "alice", 2); // previous contributes 2; room for 2
        clock.millis = 120_000;
        exhaust(limiter, "alice", 2); // only the last window's 2 are carried forward
    }

    @Test
    void includesIncomingRequestRatherThanAllowingFractionalOvershoot() {
        provider.configure("alice", new RateLimitConfig(2, 1_000));
        clock.millis = 999;
        exhaust(limiter, "alice", 2);
        clock.millis = 1_499;
        assertFalse(limiter.allowRequest("alice")); // 1.002 + 1 > 2
        clock.millis = 1_500;
        assertTrue(limiter.allowRequest("alice")); // 1 + 1 == 2
        assertFalse(limiter.allowRequest("alice"));
    }

    @Test
    void skippedWindowsDiscardBothOldCounters() {
        exhaust(limiter, "alice", 4);
        clock.millis = 120_000;
        exhaust(limiter, "alice", 4);
    }

    @Test
    void updatesResetOnlyTheirUserIncludingEqualAndReusedSettings() {
        var config = new RateLimitConfig(1, 60_000);
        provider.configure("alice", config);
        exhaust(limiter, "alice", 1);
        exhaust(limiter, "bob", 4);
        provider.configure("alice", config);
        exhaust(limiter, "alice", 1);
        provider.configure("alice", new RateLimitConfig(5, 120_000));
        provider.configure("alice", config);
        exhaust(limiter, "alice", 1);
        assertFalse(limiter.allowRequest("bob"));
    }

}
