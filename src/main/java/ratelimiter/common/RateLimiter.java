package ratelimiter.common;

/** Admission contract shared by fixed window, sliding window, and token bucket algorithms. */
public interface RateLimiter {
    /**
     * Returns true if this request is admitted; every call is a separate request.
     * @throws NullPointerException if userId is null
     * @throws IllegalArgumentException if userId is blank
     */
    boolean allowRequest(String userId);
}
