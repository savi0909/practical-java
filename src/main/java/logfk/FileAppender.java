package logfk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/** Buffered UTF-8 append-mode file destination. Buffer size is measured in characters. */
public final class FileAppender extends WriterAppender {
    public FileAppender(Level threshold, Layout layout, Path path, int bufferSize, boolean immediateFlush)
            throws IOException {
        super(Objects.requireNonNull(threshold, "threshold"), Objects.requireNonNull(layout, "layout"),
                open(path, bufferSize), immediateFlush, true);
    }

    private static Writer open(Path path, int bufferSize) throws IOException {
        Objects.requireNonNull(path, "path");
        if (bufferSize <= 0) throw new IllegalArgumentException("bufferSize must be positive");
        Path absolute = path.toAbsolutePath().normalize();
        Files.createDirectories(absolute.getParent());
        return new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(absolute,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND), StandardCharsets.UTF_8), bufferSize);
    }
}
