package magma;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    public static final Path SOURCE = Paths.get(".", "temp.java");
    public static final Path TARGET = Paths.get(".", "temp.c");

    private static void run() throws IOException, CompileException {
        if (!Files.exists(SOURCE)) return;

        final var input = Files.readString(SOURCE);
        final var output = input.isEmpty() ? "" : compileInput(input);
        Files.writeString(TARGET, output);
    }

    private static String compileInput(String root) throws CompileException {
        if (root.equals(renderPackage())) return "";
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

    private static String renderPackage() {
        return "package temp;";
    }

    @Test
    void packageStatement() throws IOException {
        runWithInput(renderPackage());
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
