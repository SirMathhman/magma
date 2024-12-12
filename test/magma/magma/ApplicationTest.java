package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTest {
    private static Result<String, RuntimeError> run(String input) {
        if (input.isEmpty()) return new Ok<>("");

        try {
            Integer.parseInt(input);
            return new Ok<>(input);
        } catch (NumberFormatException e) {
            return new Err<>(new RuntimeError());
        }
    }

    private static void assertRun(String input) {
        assertEquals(input, run(input).findValue().orElseThrow());
    }

    @Test
    void empty() {
        assertRun("");
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void numeric(String value) {
        assertRun(value);
    }

    @Test
    void invalidSymbol() {
        assertTrue(run("x").isErr());
    }
}
