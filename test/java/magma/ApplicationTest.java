package magma;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    public static final Path TARGET = resolveByExtension(Application.MAGMA_EXTENSION);
    public static final Path SOURCE = resolveByExtension(Application.EXTENSION_SEPARATOR + "java");

    private static Path resolveByExtension(String extension) {
        return Paths.get(".").resolve("ApplicationTest" + extension);
    }

    private static void runOrFail() {
        new Application(SOURCE).run().ifPresent(Assertions::fail);
    }

    private static void runWithInput(String input) {
        Application.writeSafe(SOURCE, input).ifPresent(Assertions::fail);
        runOrFail();
    }

    private static void assertRun(String input, String output) {
        runWithInput(input);
        Application.readSafe(TARGET).consume(value -> assertEquals(output, value), Assertions::fail);
    }

    @Test
    void importStatement() {
        final var value = Application.renderImport();
        assertRun(value, value);
    }

    @Test
    void packageStatement() {
        assertRun("package magma;", "");
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
        runWithInput("");
        assertTrue(Files.exists(TARGET));
    }

    @Test
    void generatesNoTarget() {
        assertFalse(Files.exists(TARGET));
    }
}
