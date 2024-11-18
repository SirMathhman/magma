package magma;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    private static void run() throws IOException {
        if (!Files.exists(SOURCE)) return;

        final var input = Files.readString(SOURCE);
        final var output = Compiler.compile(input);
        Files.writeString(TARGET, output);
    }

    private static void assertRun(String input, String output) {
        try {
            Files.writeString(SOURCE, input);
            run();
            assertEquals(output, Files.readString(TARGET));
        } catch (IOException e) {
            fail(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void packageAndImport(String namespace) {
        final var renderedImport = Compiler.renderImport(namespace);
        assertRun(Compiler.renderPackageStatement(namespace) + renderedImport, renderedImport);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void packageStatement(String namespace) {
        assertRun(Compiler.renderPackageStatement(namespace), "");
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void importStatement(String namespace) {
        assertRun(Compiler.renderImport(namespace), Compiler.renderImport(namespace));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TARGET);
        Files.deleteIfExists(SOURCE);
    }

    @Test
    void generatesNoTarget() throws IOException {
        run();
        assertFalse(Files.exists(TARGET));
    }

    @Test
    void generatesTarget() throws IOException {
        Files.createFile(SOURCE);
        run();
        assertTrue(Files.exists(TARGET));
    }
}
