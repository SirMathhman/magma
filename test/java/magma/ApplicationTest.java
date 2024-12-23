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
    public static final Path SOURCE = Paths.get(".", "temp.java");
    public static final Path TARGET = Paths.get(".", "temp.c");

    private static void run() throws IOException {
        if (Files.exists(SOURCE)) {
            Files.createFile(TARGET);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TARGET);
        Files.deleteIfExists(SOURCE);
    }

    @Test
    void generateNothing() throws IOException {
        run();
        assertFalse(Files.exists(TARGET));
    }

    @Test
    void generateSomething() throws IOException {
        Files.createFile(SOURCE);
        run();
        assertTrue(Files.exists(TARGET));
    }
}
