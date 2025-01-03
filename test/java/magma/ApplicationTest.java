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

    @Test
    void empty() throws IOException {
        new Application(new SingleSourceSet(SOURCE_PATH)).run();
        assertFalse(doesTargetExist());
    }

    @Test
    void temp() throws IOException {
        Files.createFile(SOURCE_PATH);
        new Application(new SingleSourceSet(SOURCE_PATH)).run();
        assertTrue(doesTargetExist());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TARGET_PATH);
        Files.deleteIfExists(SOURCE_PATH);
    }
}
