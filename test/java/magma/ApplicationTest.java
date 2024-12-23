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
    public static final Path SOURCE = Paths.get(".", "temp.java");
    public static final Path TARGET = Paths.get(".", "temp.c");
    public static final String PACKAGE_KEYWORD_WITH_SPACE = "package ";
    public static final String STATEMENT_END = ";";

    private static void run() throws IOException, CompileException {
        if (!Files.exists(SOURCE)) return;

        final var input = Files.readString(SOURCE);
        final var output = input.isEmpty() ? "" : compileInput(input);
        Files.writeString(TARGET, output);
    }

    private static String compileInput(String root) throws CompileException {
        if (root.startsWith(PACKAGE_KEYWORD_WITH_SPACE) && root.endsWith(STATEMENT_END)) return "";
        throw new CompileException("Unknown root", root);
    }

    private static void runWithInput(String input) {
        try {
            runWithInputExceptionally(input);
        } catch (IOException | CompileException e) {
            fail(e);
        }
    }

    private static void runWithInputExceptionally(String input) throws IOException, CompileException {
        Files.writeString(SOURCE, input);
        run();
    }

    private static String renderPackage(String namespace) {
        return PACKAGE_KEYWORD_WITH_SPACE + namespace + STATEMENT_END;
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void packageStatement(String namespace) throws IOException {
        runWithInput(renderPackage(namespace));
        assertEquals("", Files.readString(TARGET));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TARGET);
        Files.deleteIfExists(SOURCE);
    }

    @Test
    void invalidate() {
        assertThrows(CompileException.class, () -> runWithInputExceptionally("test"));
    }

    @Test
    void generateNothing() throws IOException, CompileException {
        run();
        assertFalse(Files.exists(TARGET));
    }

    @Test
    void generateSomething() throws IOException, CompileException {
        runWithInput("");
        assertTrue(Files.exists(TARGET));
    }
}
