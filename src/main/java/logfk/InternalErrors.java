package logfk;

/** Suppresses diagnostic-handler recursion across all contexts on the current thread. */
final class InternalErrors {
    private static final ThreadLocal<Boolean> REPORTING = ThreadLocal.withInitial(() -> false);
    private InternalErrors() { }
    static boolean isReporting() { return REPORTING.get(); }

    static void report(ErrorHandler handler, String operation, Exception failure) {
        if (isReporting()) return;
        REPORTING.set(true);
        try { handler.onError(operation, failure); }
        catch (RuntimeException ignored) { /* A broken diagnostic sink cannot break application logging. */ }
        finally { REPORTING.remove(); }
    }
}
