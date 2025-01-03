package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record Application(SourceSet sourceSet) {
    private static void runWithSource(Path source) throws IOException {
        final var fullName = source.getFileName().toString();
        final var separator = fullName.indexOf('.');
        if (separator == -1) return;

        final var name = fullName.substring(0, separator);
        Files.createFile(source.resolveSibling(name + ".c"));
    }

    void run() throws IOException {
        final var sources = sourceSet().collect();
        for (var source : sources) {
            runWithSource(source);
        }
    }
}