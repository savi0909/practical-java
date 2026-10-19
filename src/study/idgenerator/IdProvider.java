package study.idgenerator;

public interface IdProvider {
    /** Unique per shared instance, increasing in allocation order; throws when exhausted. */
    long getId();

    /** Comparison only: independent sequence; do not mix with getId() for uniqueness. */
    long getSynchronizedId();

    /** Comparison only: a third, independent sequence protected by ReentrantLock. */
    long getLockedId();
}
