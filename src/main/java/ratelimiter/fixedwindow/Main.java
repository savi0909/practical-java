package ratelimiter.fixedwindow;

import ratelimiter.common.InMemoryRateLimitConfigProvider;
import ratelimiter.common.RateLimitConfig;
import ratelimiter.common.RateLimiter;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public class Main {
    public static void main(String[] args) {
        // Freeze time for deterministic output; use Clock.systemUTC() in an application.
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1_000), ZoneOffset.UTC);
        var provider = new InMemoryRateLimitConfigProvider(new RateLimitConfig(5, 60_000));
        provider.configure("user-A", new RateLimitConfig(3, 10_000));
        provider.configure("user-B", new RateLimitConfig(10, 60_000));
        RateLimiter limiter = new FixedWindowRateLimiter(provider, clock);
        for (int i = 1; i <= 5; i++) {
            System.out.println("user-A request " + i + ": " + limiter.allowRequest("user-A"));
        }
        System.out.println("user-B: " + limiter.allowRequest("user-B"));
        System.out.println("Default user: " + limiter.allowRequest("user-C"));
        provider.configure("user-A", new RateLimitConfig(1, 60_000));
        System.out.println("user-A after config change: " + limiter.allowRequest("user-A"));
        System.out.println("user-A quota exhausted: " + limiter.allowRequest("user-A"));
    }
}
