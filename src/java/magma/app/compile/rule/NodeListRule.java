package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.GenerateException;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record NodeListRule(String propertyKey, Rule childRule) implements Rule {
    static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';') {
            return appended.advance();
        } else {
            return appended;
        }
    }

    static List<String> split(String input) {
        final var length = input.length();
        var state = new State();

        for (int i = 0; i < length; i++) {
            final var c = input.charAt(i);
            state = splitAtChar(state, c);
        }

        return state.advance().segments;
    }

    private Result<Node, ParseException> parse1(String input) {
        final var segments = split(input);

        Result<List<Node>, ParseException> children = new Ok<>(new ArrayList<>());
        for (String segment : segments) {
            children = children.and(() -> childRule.parse(segment).first().orElseThrow()).mapValue(tuple -> {
                final var copy = new ArrayList<>(tuple.left());
                copy.add(tuple.right());
                return copy;
            });
        }

        return children.mapValue(list -> new MapNode().withNodeList(propertyKey, list));
    }

    private Result<String, GenerateException> generate1(Node node) {

        final var propertyValues = node.findNodeList(this.propertyKey());
        if (propertyValues.isEmpty())
            return new Err<>(new GenerateException("Node list property '" + propertyKey + "' not present", node));

        Result<StringBuilder, GenerateException> buffer = new Ok<>(new StringBuilder());
        for (var value : propertyValues.get()) {
            buffer = buffer.and(() -> this.childRule().generate(value).first().orElseThrow()).mapValue(tuple -> {
                tuple.left().append(tuple.right());
                return tuple.left();
            });
        }

        return buffer.mapValue(StringBuilder::toString);
    }

    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        return new RuleResult<>(Collections.singletonList(parse1(input)));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        return new RuleResult<>(Collections.singletonList(generate1(node)));
    }

    static class State {
        private final List<String> segments;
        private final StringBuilder buffer;

        private State() {
            this(new ArrayList<>(), new StringBuilder());
        }

        private State(List<String> segments, StringBuilder buffer) {
            this.buffer = buffer;
            this.segments = segments;
        }

        private State append(char c) {
            return new State(segments, buffer.append(c));
        }

        private State advance() {
            if (buffer.isEmpty()) return this;

            final var copy = new ArrayList<>(segments);
            copy.add(buffer.toString());
            return new State(copy, new StringBuilder());
        }
    }
}