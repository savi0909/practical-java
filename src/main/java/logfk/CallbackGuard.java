package logfk;

/** Shared by caller and worker threads to prevent callbacks reentering any context. */
final class CallbackGuard {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);
    private CallbackGuard() { }
    static boolean active() { return ACTIVE.get(); }
    static void enter() { ACTIVE.set(true); }
    static void exit() { ACTIVE.remove(); }
    static void rejectControlOperation() {
        if (active() || InternalErrors.isReporting())
            throw new IllegalStateException("Cannot obtain loggers or flush/close contexts from logging callbacks");
    }
}
