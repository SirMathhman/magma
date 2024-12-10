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

        var state = new Scope();
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

    private static Tuple<Scope, String> compileRootMember(Scope scope, String input) {
        final var or = parseRootMember(input);
        if (or.isEmpty()) return new Tuple<>(scope, "");
        final var node = or.get();
        return evaluateRootMember(scope, node);
    }

    private static Tuple<Scope, String> evaluateRootMember(Scope scope, Node node) {
        return evaluateDefinition(scope, node)
                .or(() -> evaluateReturn(scope, node))
                .or(() -> evaluateNumeric(scope, node))
                .orElseGet(() -> new Tuple<>(scope, ""));
    }

    private static Optional<Tuple<Scope, String>> evaluateNumeric(Scope scope, Node node) {
        if (!node.is("numeric")) return Optional.empty();

        final var value = node.findString("value").orElse("");
        return Optional.of(new Tuple<>(scope, value));
    }

    private static Optional<Tuple<Scope, String>> evaluateDefinition(Scope scope, Node node) {
        if (!node.is("definition")) return Optional.empty();

        final var name1 = node.findString("name").orElse("");
        final var value1 = node.findString("value").orElse("");
        final var defined = scope.define(name1, value1);
        return Optional.of(new Tuple<>(defined, ""));
    }

    private static Optional<Tuple<Scope, String>> evaluateReturn(Scope scope, Node node) {
        if (!node.is("return")) return Optional.empty();

        final var symbol = node.findString("value").orElse("");
        var result = scope.find(symbol).orElse(symbol);
        return Optional.of(new Tuple<>(scope, result));
    }

    private static Optional<Node> parseRootMember(String input) {
        return createDefinitionRule().parse(input)
                .or(() -> createReturnRule().parse(input))
                .or(() -> createNumericRule().parse(input));
    }

    private static TypeRule createNumericRule() {
        return new TypeRule("numeric", new StringRule("value"));
    }

    private static Rule createDefinitionRule() {
        final var name = new StringRule("name");
        final var value = new StringRule("value");

        final var withEnd = new SuffixRule(value, STATEMENT_END);
        return new TypeRule("definition", new PrefixRule(LET_KEYWORD_WITH_SPACE, new InfixRule(name, ASSIGNMENT_OPERATOR, withEnd)));
    }

    private static Rule createReturnRule() {
        final var value = new StringRule("value");
        return new TypeRule("return", new PrefixRule(RETURN_KEYWORD_WITH_SPACE, new SuffixRule(value, STATEMENT_END)));
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
        assertRun(generateDefinition("test", value) + generateReturn("test"), value);
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

    private record Scope(Map<String, String> frame) {
        public Scope() {
            this(new HashMap<>());
        }


        public Scope define(String name, String value) {
            frame.put(name, value);
            return this;
        }

        public Optional<String> find(String name) {
            return Optional.ofNullable(frame.get(name));
        }
    }
}
