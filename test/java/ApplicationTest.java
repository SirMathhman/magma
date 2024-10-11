import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    public static final Path SOURCE = resolve("java");
    public static final Path TARGET = resolve("mgs");

    private static Path resolve(String extension) {
        return Paths.get(".", "ApplicationTest." + extension);
    }

    private static void runMaybeFail() {
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

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TARGET);
        Files.deleteIfExists(SOURCE);
    }

    @Test
    void generatesNoTarget() {
        runMaybeFail();
        assertFalse(Files.exists(TARGET));
    }

    @Test
    void generatesTarget() {
        try {
            Files.createFile(SOURCE);
        } catch (IOException e) {
            fail(e);
        }

        runMaybeFail();
        assertTrue(Files.exists(TARGET));
    }
}