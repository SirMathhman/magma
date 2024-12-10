package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTest {
    public static final String RETURN_KEYWORD_WITH_SPACE = "return ";
    public static final String STATEMENT_END = ";";
    public static final String ASSIGNMENT_OPERATOR = "=";
    public static final String LET_KEYWORD_WITH_SPACE = "let ";

    private static String generateReturn(String value) {
        return RETURN_KEYWORD_WITH_SPACE + value + STATEMENT_END;
    }

    private static String run(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            buffer.append(c);
            if (c == ';') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        advance(buffer, segments);

        var state = new State();
        var output = new StringBuilder();
        for (String segment : segments) {
            final var tuple = compileRootMember(state, segment);
            state = tuple.left();
            output.append(tuple.right());
        }

        return output.toString();
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static Tuple<State, String> compileRootMember(State state, String input) {
        if (input.startsWith(LET_KEYWORD_WITH_SPACE)) {
            final var slice = input.substring(LET_KEYWORD_WITH_SPACE.length());
            final var operator = slice.indexOf(ASSIGNMENT_OPERATOR);
            if (operator != -1) {
                final var name = slice.substring(0, operator);
                final var withEnd = slice.substring(operator + 1);
                if (withEnd.endsWith(STATEMENT_END)) {
                    final var value = withEnd.substring(0, withEnd.length() - STATEMENT_END.length());
                    final var defined = state.define(name, value);
                    return new Tuple<>(defined, "");
                }
            }
        }

        if (input.startsWith(RETURN_KEYWORD_WITH_SPACE)) {
            final var afterKeyword = input.substring(RETURN_KEYWORD_WITH_SPACE.length());
            if (afterKeyword.endsWith(STATEMENT_END)) {
                final var slice = afterKeyword.substring(0, afterKeyword.length() - STATEMENT_END.length());
                var result = state.find(slice).orElse(slice);
                return new Tuple<>(state, result);
            }
        }

        return new Tuple<>(state, input);
    }

    private static String generateDefinition(String name, String value) {
        return LET_KEYWORD_WITH_SPACE + name + ASSIGNMENT_OPERATOR + value + STATEMENT_END;
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
        assertRun(generateDefinition("fram", value) + generateReturn("fram"), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void definitionName(String name) {
        assertRun(generateDefinition(name, "100") + generateReturn(name), "100");
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void definitionNameValid(String name) {
        assertRun(generateDefinition("first", String.valueOf("first".length()))
                + generateDefinition("second", String.valueOf("second".length()))
                + generateReturn(name), String.valueOf(name.length()));
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

    private record State(Map<String, String> frame) {
        public State() {
            this(new HashMap<>());
        }


        public State define(String name, String value) {
            frame.put(name, value);
            return this;
        }

        public Optional<String> find(String name) {
            return Optional.ofNullable(frame.get(name));
        }
    }
}
