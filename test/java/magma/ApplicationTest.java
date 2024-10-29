package magma;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    public static final Path TARGET = Paths.get(".", "ApplicationTest.mgs");
    public static final Path SOURCE = Paths.get(".", "ApplicationTest.java");

    private static void runOrFail() {
        try {
            run();
        } catch (IOException e) {
            fail(e);
        }
    }

    private static void run() throws IOException {
        if (Files.exists(SOURCE)) {
            Files.createFile(TARGET);
        }
    }

    private static void createSource() {
        try {
            Files.createFile(SOURCE);
        } catch (IOException e) {
            fail(e);
        }
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(TARGET);
            Files.deleteIfExists(SOURCE);
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void generatesTarget() {
        createSource();
        runOrFail();
        assertTrue(Files.exists(TARGET));
    }

    @Test
    void generatesNoTarget() {
        assertFalse(Files.exists(TARGET));
    }
}
