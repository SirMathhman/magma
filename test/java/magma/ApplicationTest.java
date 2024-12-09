package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTest {
    public static final String RETURN_KEYWORD_WITH_SPACE = "return ";
    public static final String STATEMENT_END = ";";

    private static String run(String input) {
        if (input.startsWith(RETURN_KEYWORD_WITH_SPACE) && input.endsWith(STATEMENT_END)) {
            return input.substring(RETURN_KEYWORD_WITH_SPACE.length(), input.length() - STATEMENT_END.length());
        }

        return input;
    }

    @Test
    void empty() {
        assertEquals("", run(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void value(String value) {
        assertEquals(value, run(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void returns(String value) {
        assertEquals(value, run(RETURN_KEYWORD_WITH_SPACE + value + STATEMENT_END));
    }
}
