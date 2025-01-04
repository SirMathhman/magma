package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record Application(SourceSet sourceSet) {
    private static void runWithSource(Path source) throws IOException {
        final var name = source.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));
        Files.createFile(source.resolveSibling(nameWithoutExt + ".c"));
    }

    void run() throws IOException {
        final var sources = sourceSet().collect();

        for (Path source : sources) {
            runWithSource(source);
        }
    }
}