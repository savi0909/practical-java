package ratelimiter;

import java.util.Map;

/** Small client example: each use case owns a reusable algorithm instance. */
public final class RateLimiterExample {
    public static void main(String[] args) {
        RateLimiterManager loginLimiter = new RateLimiterManager(5, 60_000);
        loginLimiter.registerUser("alice", 3, 60_000);

        // Configure once. Recreating these on each request would reset quotas.
        Map<String, RateLimiter> useCases = Map.of(
                "login", loginLimiter,
                "search", new TokenBucketRateLimiter(20, 5));

        // The same user has separate state for login and search because these
        // use cases own different limiter instances. Client code uses one contract.
        System.out.println("Login allowed: " + isAllowed(useCases, "login", "alice"));
        System.out.println("Search allowed: " + isAllowed(useCases, "search", "alice"));
    }

    private static boolean isAllowed(Map<String, RateLimiter> useCases, String useCase, String userId) {
        RateLimiter limiter = useCases.get(useCase);
        if (limiter == null) {
            throw new IllegalArgumentException("Unknown use case: " + useCase);
        }
        return limiter.isAllowed(userId);
    }
}
