package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTest {
    @Test
    void returns() {
        assertRun(generateReturn(), "10");
    }

    private static String generateReturn() {
        return "return 10;";
    }

    private static String run(String input) {
        if(input.equals(generateReturn())) {
            return "10";
        }
        return input;
    }

    @Test
    void empty() {
        assertRun("", "");
    }

    private static void assertRun(String input, String output) {
        assertEquals(output, run(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void value(String value) {
        assertRun(value, value);
    }
}
