package magma.compile.rule;

import magma.compile.Node;
import magma.ApplicationError;
import magma.java.JavaList;
import magma.api.result.Result;
import magma.api.stream.Streams;

import java.util.List;

public final class SplitRule {
    private final Rule childRule;
    private final String propertyKey;

    public SplitRule(String propertyKey, Rule childRule) {
        this.childRule = childRule;
        this.propertyKey = propertyKey;
    }

    public static List<String> split(String input) {
        var state = new State();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            var appended = state.append(c);
            state = splitAtChar(appended, c);
        }

        return state.advance().segments();
    }

    private static State splitAtChar(State state, char c) {
        if (c == ';' && state.isLevel()) return state.advance();
        if (c == '{') return state.enter();
        if (c == '}') return state.exit();
        return state;
    }

    public Result<Node, ApplicationError> parse(String input) {
        final var segments = split(input);

        return Streams.from(segments)
                .foldLeftIntoResult(new JavaList<Node>(), (list, segment) -> childRule.parse(segment).mapValue(list::add))
                .mapErr(ApplicationError::new)
                .mapValue(list -> new Node().withNodeList(propertyKey, list));
    }

    public Result<String, ApplicationError> generate(Node value) {
        return value.findNodeList(propertyKey)
                .orElse(new JavaList<Node>())
                .stream().foldLeftIntoResult(new StringBuilder(), (builder, segment) -> childRule.generate(segment)
                        .mapValue(builder::append))
                .mapValue(StringBuilder::toString)
                .mapErr(ApplicationError::new);
    }
}