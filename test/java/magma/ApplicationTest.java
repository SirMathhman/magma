package magma;

import magma.compile.Compiler;
import magma.compile.lang.CommonLang;
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

    private static Path resolve(String extension) {
        return Paths.get(".", "ApplicationTest" + EXTENSION_SEPARATOR + extension);
    }

    private static void runMaybeFail() {
        try {
            run();
        } catch (ApplicationException e) {
            fail(e);
        }
    }

    private static void run() throws ApplicationException {
        if (!Files.exists(SOURCE)) return;

        final var input = readSafe();
        final var output = new Compiler(input).compile();

        final var fileName = SOURCE.getFileName().toString();
        final var separator = fileName.indexOf('.');
        if (separator == -1) return;

        final var applicationTest = fileName.substring(0, separator);
        final var targetName = applicationTest + EXTENSION_SEPARATOR + MAGMA_EXTENSION;
        final var target = SOURCE.resolveSibling(targetName);
        writeSafe(target, output);
    }

    private static void writeSafe(Path target, String output) throws ApplicationException {
        try {
            Files.writeString(target, output);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private static String readSafe() throws ApplicationException {
        try {
            return Files.readString(SOURCE);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private static String renderNamespaceStatement(String prefix, String namespace) {
        return prefix + namespace + CommonLang.STATEMENT_END;
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
        final var packageStatement = renderNamespaceStatement("package ", "namespace");
        final var importStatement = renderNamespaceStatement("import ", "test");
        assertRun(packageStatement + importStatement, importStatement);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void packageStatement(String namespace) {
        assertRun(renderNamespaceStatement("package ", namespace), "");
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void importStatement(String namespace) {
        final var content = renderNamespaceStatement("import ", namespace);
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