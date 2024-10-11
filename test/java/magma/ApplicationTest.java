package magma;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    public static final String MAGMA_EXTENSION = "mgs";
    public static final String EXTENSION_SEPARATOR = ".";
    public static final Path SOURCE = resolve("java");
    public static final Path TARGET = resolve(MAGMA_EXTENSION);
    public static final String expected = "import org.junit.jupiter.api.AfterEach;";

    private static Path resolve(String extension) {
        return Paths.get(".", "ApplicationTest" + EXTENSION_SEPARATOR + extension);
    }

    private static void runMaybeFail() {
        try {
            run();
        } catch (IOException e) {
            fail(e);
        }
    }

    private static void run() throws IOException {
        if (!Files.exists(SOURCE)) return;

        final var input = Files.readString(SOURCE);

        final var fileName = SOURCE.getFileName().toString();
        final var separator = fileName.indexOf('.');
        if (separator == -1) return;

        final var applicationTest = fileName.substring(0, separator);
        final var targetName = applicationTest + EXTENSION_SEPARATOR + MAGMA_EXTENSION;
        final var target = SOURCE.resolveSibling(targetName);
        Files.writeString(target, input);
    }

    private static void runWithInput(String input) {
        try {
            Files.writeString(SOURCE, input);
        } catch (IOException e) {
            fail(e);
        }

        runMaybeFail();
    }

    @Test
    void importStatement() {
        runWithInput(expected);

        try {
            final var actual = Files.readString(TARGET);
            assertEquals(expected, actual);
        } catch (IOException e) {
            fail(e);
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
        runWithInput("");
        assertTrue(Files.exists(TARGET));
    }
}