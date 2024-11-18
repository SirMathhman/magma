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
    public static final String IMPORT_KEYWORD_WITH_SPACE = "import ";
    public static final String STATEMENT_END = ";";

    private static Path resolve(String extension) {
        return Paths.get(".", "ApplicationTest." + extension);
    }

    private static void run() throws IOException {
        if (!Files.exists(SOURCE)) return;

        final var input = Files.readString(SOURCE);
        String output;
        if (input.startsWith(IMPORT_KEYWORD_WITH_SPACE) && input.endsWith(STATEMENT_END)) {
            final var namespace = input.substring(IMPORT_KEYWORD_WITH_SPACE.length(), input.length() - STATEMENT_END.length());
            output = renderImport(namespace);
        } else {
            output = "";
        }

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

    private static String renderImport(String namespace) {
        return IMPORT_KEYWORD_WITH_SPACE + namespace + STATEMENT_END;
    }

    @Test
    void packageStatement() {
        assertRun("package magma;", "");
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void importStatement(String namespace) {
        assertRun(renderImport(namespace), renderImport(namespace));
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
