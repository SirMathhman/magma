package magma;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApplicationTest {
    @Test
    void empty() {
        assertFalse(Files.exists(Paths.get(".", "Main.c")));
    }
}
