package logfk;

/** Internal diagnostics must not route through the logging pipeline. */
@FunctionalInterface
public interface ErrorHandler {
    void onError(String operation, Exception failure);

    static ErrorHandler stderr() {
        return (operation, failure) -> System.err.println("[logfk] " + operation + ": " + failure);
    }
}
