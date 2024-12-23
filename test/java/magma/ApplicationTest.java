package magma;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    public static final Path SOURCE = Paths.get(".", "temp.java");
    public static final Path TARGET = Paths.get(".", "temp.c");
    public static final String PACKAGE_KEYWORD_WITH_SPACE = "package ";
    public static final String STATEMENT_TERMINATOR = ";";
    public static final String IMPORT_KEYWORD_WITH_SPACE = "import ";

    private static void run() throws IOException, CompileException {
        if (!Files.exists(SOURCE)) return;

        final var input = Files.readString(SOURCE);
        final var output = input.isEmpty() ? "" : compileRoot(input);
        Files.writeString(TARGET, output);
    }

    private static String compileRoot(String root) throws CompileException {
        final var segments = split(root);
        var buffer = new StringBuilder();
        for (String segment : segments) {
            buffer.append(compileRootStatement(segment));
        }

        return buffer.toString();
    }

    private static ArrayList<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static String compileRootStatement(String rootStatement) throws CompileException {
        if (rootStatement.startsWith(PACKAGE_KEYWORD_WITH_SPACE) && rootStatement.endsWith(STATEMENT_TERMINATOR))
            return "";

        if (rootStatement.startsWith(IMPORT_KEYWORD_WITH_SPACE) && rootStatement.endsWith(STATEMENT_TERMINATOR))
            return rootStatement;

        throw new CompileException("Unknown root statement", rootStatement);
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
        return PACKAGE_KEYWORD_WITH_SPACE + namespace + STATEMENT_TERMINATOR;
    }

    private static void assertRun(String input, String output) {
        try {
            runWithInput(input);
            assertEquals(output, Files.readString(TARGET));
        } catch (IOException e) {
            fail(e);
        }
    }

    private static String renderImport(String namespace) {
        return IMPORT_KEYWORD_WITH_SPACE + namespace + STATEMENT_TERMINATOR;
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
