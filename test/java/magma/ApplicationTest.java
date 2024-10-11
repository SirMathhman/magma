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
    public static final String MAGMA_EXTENSION = "mgs";
    public static final String EXTENSION_SEPARATOR = ".";
    public static final Path SOURCE = resolve("java");
    public static final Path TARGET = resolve(MAGMA_EXTENSION);
    public static final String IMPORT_KEYWORD_WITH_SPACE = "import ";

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
        Files.writeString(target, new Compiler(input).compile());
    }

    private static String renderNamespaceStatement(String prefix, String namespace) {
        return prefix + namespace + Compiler.STATEMENT_END;
    }

    private static void runWithInput(String input) {
        try {
            Files.writeString(SOURCE, input);
        } catch (IOException e) {
            fail(e);
        }

        runMaybeFail();
    }

    private static void assertRun(String input, String output) {
        runWithInput(input);
        assertOutput(output);
    }

    private static void assertOutput(String expected) {
        try {
            final var actual = Files.readString(TARGET);
            assertEquals(expected, actual);
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void packageAndImport() {
        final var packageStatement = renderNamespaceStatement(Compiler.PACKAGE_KEYWORD_WITH_SPACE, "namespace");
        final var importStatement = renderNamespaceStatement(IMPORT_KEYWORD_WITH_SPACE, "test");
        assertRun(packageStatement + importStatement, importStatement);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void packageStatement(String namespace) {
        assertRun(renderNamespaceStatement(Compiler.PACKAGE_KEYWORD_WITH_SPACE, namespace), "");
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void importStatement(String namespace) {
        final var content = renderNamespaceStatement(IMPORT_KEYWORD_WITH_SPACE, namespace);
        assertRun(content, content);
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