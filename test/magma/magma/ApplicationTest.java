package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTest {

    public static final String RETURN_KEYWORD_WITH_SPACE = "return ";
    public static final String STATEMENT_END = ";";

    private static Result<String, RuntimeError> run(String input) {
        if (input.isEmpty()) return new Ok<>("");
        if (input.startsWith(RETURN_KEYWORD_WITH_SPACE)) {
            final var slice = input.substring(RETURN_KEYWORD_WITH_SPACE.length());
            if(slice.endsWith(STATEMENT_END)) {
                return new Ok<>(slice.substring(0, slice.length() - STATEMENT_END.length()));
            }
        }

        try {
            Integer.parseInt(input);
            return new Ok<>(input);
        } catch (NumberFormatException e) {
            return new Err<>(new RuntimeError());
        }
    }

    private static void assertRun(String output) {
        assertRun(output, output);
    }

    private static void assertRun(String output, String input) {
        assertEquals(output, run(input).findValue().orElseThrow());
    }

    private static String renderReturn(String value) {
        return RETURN_KEYWORD_WITH_SPACE + value + STATEMENT_END;
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

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void returns(String value) {
        assertRun(value, renderReturn(value));
    }
}
