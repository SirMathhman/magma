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

    private static boolean doesTargetExist() {
        return Files.exists(TARGET_PATH);
    }

    private static void run() throws IOException {
        if (Files.exists(SOURCE_PATH)) {
            Files.createFile(TARGET_PATH);
        }
    }

    @Test
    void empty() throws IOException {
        run();
        assertFalse(doesTargetExist());
    }

    @Test
    void temp() throws IOException {
        Files.createFile(SOURCE_PATH);
        run();
        assertTrue(doesTargetExist());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TARGET_PATH);
        Files.deleteIfExists(SOURCE_PATH);
    }
}
