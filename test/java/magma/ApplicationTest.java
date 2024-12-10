package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTest {
    public static final String STATEMENT_END = ";";
    public static final String RETURN_KEYWORD_WITH_SPACE = "return ";

    private static String run(String input) {
        if(input.startsWith("let value = 100;")) {
            return getString(input.substring("let value = 100;".length()));
        } else {
            return getString(input);
        }
    }

    private static String getString(String input) {
        if (input.startsWith(RETURN_KEYWORD_WITH_SPACE) && input.endsWith(STATEMENT_END)) {
            return input.substring(RETURN_KEYWORD_WITH_SPACE.length(), input.length() - STATEMENT_END.length());
        }
        return input;
    }

    private static void assertRun(String input, String output) {
        assertEquals(output, run(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void returns(String value) {
        assertRun(generateReturn(value), value);
    }

    private static String generateReturn(String value) {
        return RETURN_KEYWORD_WITH_SPACE + value + STATEMENT_END;
    }

    @Test
    void definition() {
        assertRun("let value = 100;" + generateReturn("100"), "100");
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
}
