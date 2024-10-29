package magma;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
        run().ifPresent(Assertions::fail);
    }

    private static Option<IOException> run() {
        if (!Files.exists(SOURCE)) return new None<>();

        final var fileName = SOURCE.getFileName().toString();
        final var nameWithoutExtension = new JavaString(fileName)
                .firstIndexOfChar(EXTENSION_SEPARATOR)
                .map(index -> fileName.substring(0, index))
                .orElse(fileName);

        try {
            Files.createFile(SOURCE.resolveSibling(nameWithoutExtension + MAGMA_EXTENSION));
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }

    private static void writeSource(String input) {
        try {
            Files.writeString(SOURCE, input);
        } catch (IOException e) {
            fail(e);
        }
    }

    private static void runWithInput(String input) {
        writeSource(input);
        runOrFail();
    }

    private static Result<String, IOException> readSafe() {
        try {
            return new Ok<>(Files.readString(TARGET));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    @Test
    void packageStatement() {
        runWithInput("package magma;");
        readSafe().consume(value -> assertEquals("", value), Assertions::fail);
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
