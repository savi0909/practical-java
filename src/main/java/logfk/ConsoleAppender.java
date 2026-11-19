package logfk;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Objects;

/** Whole-record writes to an injected PrintStream; never closes the console stream. */
public final class ConsoleAppender extends WriterAppender {
    public ConsoleAppender(Level threshold, Layout layout, PrintStream stream) {
        super(threshold, layout, checkedWriter(Objects.requireNonNull(stream, "stream")), true, false);
    }

    private static Writer checkedWriter(PrintStream stream) {
        return new Writer() {
            @Override public void write(char[] chars, int offset, int length) throws IOException {
                stream.print(new String(chars, offset, length));
                check();
            }
            @Override public void flush() throws IOException { stream.flush(); check(); }
            @Override public void close() throws IOException { flush(); }
            private void check() throws IOException {
                if (stream.checkError()) throw new IOException("Console stream reported a write failure");
            }
        };
    }
}
