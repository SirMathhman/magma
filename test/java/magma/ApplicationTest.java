package magma;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    public static final char EXTENSION_SEPARATOR = '.';
    public static final String MAGMA_EXTENSION = EXTENSION_SEPARATOR + "mgs";
    public static final Path TARGET = resolveByExtension(MAGMA_EXTENSION);
    public static final Path SOURCE = resolveByExtension(EXTENSION_SEPARATOR + "java");

    private static Path resolveByExtension(String extension) {
        return Paths.get(".").resolve("ApplicationTest" + extension);
    }

    private static void runOrFail() {
        try {
            run();
        } catch (IOException e) {
            fail(e);
        }
    }

    private static void run() throws IOException {
        if (!Files.exists(SOURCE)) return;

        final var fileName = SOURCE.getFileName().toString();
        final var nameWithoutExtension = new JavaString(fileName)
                .firstIndexOfChar(EXTENSION_SEPARATOR)
                .map(index -> fileName.substring(0, index))
                .orElse(fileName);

        Files.createFile(SOURCE.resolveSibling(nameWithoutExtension + MAGMA_EXTENSION));
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
