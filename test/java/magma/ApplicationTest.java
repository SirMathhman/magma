package magma;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTest {
    @Test
    void empty() {
        assertEquals("", run(""));
    }

    @Test
    void value() {
        assertEquals("100", run("100"));
    }

    private static String run(String input) {
        return input;
    }
}
