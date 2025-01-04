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
    public static final Path SOURCE_PATH = Paths.get(".", "Main.java");
    public static final Path TARGET_PATH = Paths.get(".", "Main.c");

    private static void run() throws IOException {
        if (!Files.exists(SOURCE_PATH)) return;

        final var name = SOURCE_PATH.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));
        Files.createFile(SOURCE_PATH.resolveSibling(nameWithoutExt + ".c"));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TARGET_PATH);
        Files.deleteIfExists(SOURCE_PATH);
    }

    @Test
    void generateTarget() throws IOException {
        Files.createFile(SOURCE_PATH);
        run();
        assertTrue(Files.exists(TARGET_PATH));
    }

    @Test
    void empty() {
        assertFalse(Files.exists(TARGET_PATH));
    }
}
