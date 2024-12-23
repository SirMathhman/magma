package magma;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    private static void runWithInput(String input) {
        try {
            runWithInputExceptionally(input);
        } catch (IOException | CompileException e) {
            fail(e);
        }
    }

    private static void runWithInputExceptionally(String input) throws IOException, CompileException {
        Files.writeString(Application.SOURCE, input);
        Application.run(Application.SOURCE);
    }

    private static String renderPackage(String namespace) {
        return Application.PACKAGE_KEYWORD_WITH_SPACE + namespace + Application.STATEMENT_TERMINATOR;
    }

    private static void assertRun(String input, String output) {
        try {
            runWithInput(input);
            assertEquals(output, Files.readString(Application.TARGET));
        } catch (IOException e) {
            fail(e);
        }
    }

    private static String renderImport(String namespace) {
        return Application.IMPORT_KEYWORD_WITH_SPACE + namespace + Application.STATEMENT_TERMINATOR;
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void importStatement(String namespace) {
        assertRun(renderImport(namespace), renderImport(namespace));
    }

    @Test
    void multipleStatements() {
        final var generatedImport = renderImport("test");
        assertRun(renderPackage("test") + generatedImport, generatedImport);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void packageStatement(String namespace) {
        assertRun(renderPackage(namespace), "");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Application.TARGET);
        Files.deleteIfExists(Application.SOURCE);
    }

    @Test
    void invalidate() {
        assertThrows(CompileException.class, () -> runWithInputExceptionally("test"));
    }

    @Test
    void generateNothing() throws IOException, CompileException {
        Application.run(Application.SOURCE);
        assertFalse(Files.exists(Application.TARGET));
    }

    @Test
    void generateSomething() throws CompileException {
        runWithInput("");
        assertTrue(Files.exists(Application.TARGET));
    }
}
