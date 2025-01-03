package magma;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTest {
    public static final Path SOURCE_PATH = resolve("java");
    public static final Path TARGET_PATH = resolve("c");

    private static Path resolve(String extension) {
        return Paths.get(".", "Main." + extension);
    }

    private static boolean doesTargetExist() {
        return Files.exists(TARGET_PATH);
    }

    private static void run(SourceSet sourceSet) throws IOException {
        final var sources = sourceSet.collect();
        for (var source : sources) {
            runWithSource(source);
        }
    }

    private static void runWithSource(Path source) throws IOException {
        final var fullName = source.getFileName().toString();
        final var separator = fullName.indexOf('.');
        if (separator == -1) return;

        final var name = fullName.substring(0, separator);
        Files.createFile(source.resolveSibling(name + ".c"));
    }

    @Test
    void empty() throws IOException {
        run(new SingleSourceSet(SOURCE_PATH));
        assertFalse(doesTargetExist());
    }

    @Test
    void temp() throws IOException {
        Files.createFile(SOURCE_PATH);
        run(new SingleSourceSet(SOURCE_PATH));
        assertTrue(doesTargetExist());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TARGET_PATH);
        Files.deleteIfExists(SOURCE_PATH);
    }
}
