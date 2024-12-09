package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTest {
    public static final String RETURN_KEYWORD_WITH_SPACE = "return ";
    public static final String STATEMENT_END = ";";

    private static String run(String input) {
        final var separator = input.indexOf(';');
        if(separator == -1) return input;

        final var left = input.substring(0, separator + 1);
        final var right = input.substring(separator + 1);

        return executeStatement(left) + executeStatement(right);
    }

    private static String executeStatement(String input) {
        if(input.equals(generateDefinition())) return "";

        if (input.startsWith(RETURN_KEYWORD_WITH_SPACE) && input.endsWith(STATEMENT_END)) {
            return input.substring(RETURN_KEYWORD_WITH_SPACE.length(), input.length() - STATEMENT_END.length());
        }

        return input;
    }

    private static void assertRun(String input, String output) {
        assertEquals(output, run(input));
    }

    private static String generateReturn(String value) {
        return RETURN_KEYWORD_WITH_SPACE + value + STATEMENT_END;
    }

    @Test
    void empty() {
        assertRun("", "");
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void value(String value) {
        assertRun(value, value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void returns(String value) {
        assertRun(generateReturn(value), value);
    }

    @Test
    void definition() {
        assertRun(generateDefinition(), "");
    }

    private static String generateDefinition() {
        return "let value = 100;";
    }
}
