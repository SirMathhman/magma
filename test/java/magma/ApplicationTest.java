package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTest {
    public static final String STATEMENT_END = ";";
    public static final String RETURN_KEYWORD_WITH_SPACE = "return ";
    public static final String LET_KEYWORD_WITH_SPACE = "let ";
    public static final String ASSIGNMENT_OPERATOR = "=";

    private static String header(String name) {
        return LET_KEYWORD_WITH_SPACE + name + ASSIGNMENT_OPERATOR;
    }

    private static String run(String input) {
        if (!input.startsWith(LET_KEYWORD_WITH_SPACE)) return getString(input, Optional.empty());

        final var valueSeparator = input.indexOf(ASSIGNMENT_OPERATOR);
        if (valueSeparator == -1) return getString(input, Optional.empty());

        final var header = input.substring(0, valueSeparator);
        final var space = header.lastIndexOf(' ');
        final var name = header.substring(space + 1);

        final var withEnd = input.substring(valueSeparator + 1);
        final var index = withEnd.indexOf(STATEMENT_END);
        if (index == -1) return getString(input, Optional.empty());

        final var value = withEnd.substring(0, index);
        final var rootMember = input.substring(input.indexOf(';') + 1);
        return getString(rootMember, Optional.of(new Tuple<>(name, value)));
    }

    private static String getString(String input, Optional<Tuple<String, String>> optional) {
        if (!input.startsWith(RETURN_KEYWORD_WITH_SPACE) || !input.endsWith(STATEMENT_END)) return input;

        final var value = input.substring(RETURN_KEYWORD_WITH_SPACE.length(), input.length() - STATEMENT_END.length());
        if (optional.isEmpty()) return value;

        final var tuple = optional.get();
        if (tuple.left().equals(value)) {
            return tuple.right();
        }
        return value;
    }

    private static void assertRun(String input, String output) {
        assertEquals(output, run(input));
    }

    private static String generateReturn(String value) {
        return RETURN_KEYWORD_WITH_SPACE + value + STATEMENT_END;
    }

    private static String generateDefinition(String value) {
        return header("value") + value + STATEMENT_END;
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void returns(String value) {
        assertRun(generateReturn(value), value);
    }

    @Test
    void definition() {
        assertRun(generateDefinition("50") + generateReturn("100"), "100");
    }

    @Test
    void definitionValue() {
        assertRun(generateDefinition("100") + generateReturn("value"), "100");
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
