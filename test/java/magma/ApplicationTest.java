package magma;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTest {
    public static final Path SOURCE_PATH = Paths.get(".", "Main.java");
    public static final Path TARGET_PATH = Paths.get(".", "Main.c");

    private static void runWithInput(String input) throws IOException, CompileException {
        Files.writeString(SOURCE_PATH, input);
        new Application(new SingleSourceSet(SOURCE_PATH)).run();
    }

    @Test
    void invalidate() {
        assertThrows(CompileException.class, () -> runWithInput("100"));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TARGET_PATH);
        Files.deleteIfExists(SOURCE_PATH);
    }

    @Test
    void generateTarget() throws CompileException, IOException {
        runWithInput("");
        assertTrue(Files.exists(TARGET_PATH));
    }

    @Test
    void empty() {
        assertFalse(Files.exists(TARGET_PATH));
    }
}
