package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTest {

    public static final String RETURN_KEYWORD_WITH_SPACE = "return ";
    public static final String STATEMENT_END = ";";
    public static final String ASSIGNMENT_OPERATOR = "=";
    public static final String LET_KEYWORD_WITH_SPACE = "let ";

    private static String getString(String name) {
        return LET_KEYWORD_WITH_SPACE + name + ASSIGNMENT_OPERATOR;
    }

    private static String generateReturn(String value) {
        return RETURN_KEYWORD_WITH_SPACE + value + STATEMENT_END;
    }

    private static String run(String input) {
        if (input.startsWith(LET_KEYWORD_WITH_SPACE)) {
            final var slice = input.substring(LET_KEYWORD_WITH_SPACE.length());

            final var operator = slice.indexOf(ASSIGNMENT_OPERATOR);
            if (operator != -1) {
                final var withEnd = slice.substring(operator + 1);

                final var separator = withEnd.indexOf(STATEMENT_END);
                if (separator != -1) {
                    final var value = withEnd.substring(0, separator);
                    final var next = withEnd.substring(separator + 1);
                    return getString(next, Optional.of(value));
                }
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

    private static String generateDefinition(String name, String value) {
        return getString(name) + value + STATEMENT_END;
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
    void definitionValue(String value) {
        assertRun(generateDefinition("value", value) + generateReturn("value"), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void definitionName(String name) {
        assertRun(generateDefinition(name, "100") + generateReturn(name), "100");
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void returnValue(String value) {
        assertRun(generateReturn(value), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void numeric(String value) {
        assertRun(value, value);
    }
}
