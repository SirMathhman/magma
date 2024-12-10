package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTest {

    public static final String RETURN_KEYWORD_WITH_SPACE = "return ";
    public static final String STATEMENT_END = ";";
    public static final String DEFINITION_PREFIX = "let value = ";

    private static String generateReturn(String value) {
        return RETURN_KEYWORD_WITH_SPACE + value + STATEMENT_END;
    }

    private static String run(String input) {
        if (input.startsWith(DEFINITION_PREFIX)) {
            final var slice = input.substring(DEFINITION_PREFIX.length());
            final var separator = slice.indexOf(STATEMENT_END);
            if (separator != -1) {
                final var value = slice.substring(0, separator);
                final var next = slice.substring(separator + 1);
                return getString(next, Optional.of(value));
            }
        }

        return getString(input, Optional.empty());
    }

    private static String getString(String input, Optional<String> value) {
        if (!input.startsWith(RETURN_KEYWORD_WITH_SPACE)) return input;
        final var afterKeyword = input.substring(RETURN_KEYWORD_WITH_SPACE.length());

        if (!afterKeyword.endsWith(STATEMENT_END)) return input;
        final var slice = afterKeyword.substring(0, afterKeyword.length() - STATEMENT_END.length());
        return value.orElse(slice);
    }

    private static String generateDefinition(String value) {
        return DEFINITION_PREFIX + value + STATEMENT_END;
    }

    private static void assertRun(String input, String output) {
        assertEquals(output, run(input));
    }

    @Test
    void empty() {
        assertRun("", "");
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void definition(String value) {
        assertRun(generateDefinition(value) + generateReturn("value"), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void returns(String value) {
        assertRun(generateReturn(value), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void numeric(String value) {
        assertRun(value, value);
    }
}
