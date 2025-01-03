package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public record Application(SourceSet sourceSet) {
    private static void runWithSource(Source source) throws IOException {
        final var name = source.computeName();
        Files.createFile(Paths.get(".").resolve(name + ".c"));
    }

    void run() throws IOException {
        final var sources = sourceSet().collect();
        for (var source : sources) {
            runWithSource(source);
        }
    }
}