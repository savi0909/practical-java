package ratelimiter.fixedwindow;

/** Immutable counter for one epoch-aligned fixed window. */
public record WindowState(long windowId, int requestCount) {}
