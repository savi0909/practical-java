package ratelimiter;

public interface RateLimiter {

    // Choose an implementation per use case and reuse it across requests.

    /**
     * Immediately accepts and consumes one quota slot, or rejects without consuming one.
     * Each non-null user ID has an independent quota; concurrent calls must not
     * consume the same remaining slot. This does not queue or execute the request.
     */
    boolean isAllowed(String userId);

}
