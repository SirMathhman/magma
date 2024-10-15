package magma.compile.rule;

import magma.compile.GenerateException;
import magma.compile.MapNode;
import magma.compile.Node;
import magma.compile.ParseException;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private Optional<Node> parse0(String input) {
        final var segments = split(input);
        var children = new ArrayList<Node>();
        for (String segment : segments) {
            final var parsed = childRule.parse(segment).findValue();
            if (parsed.isEmpty()) return Optional.empty();
            children.add(parsed.get());
        }

        var node = new MapNode().withNodeList(propertyKey, children);
        return Optional.of(node);
    }

    private Optional<String> generate0(Node node) {
        var buffer = new StringBuilder();

        final var propertyValues = node.findNodeList(propertyKey());
        if (propertyValues.isEmpty()) return Optional.empty();

        for (var value : propertyValues.get()) {
            final var generate = this.childRule().generate(value).findValue();
            if (generate.isEmpty()) return Optional.empty();
            buffer.append(generate.get());
        }

        return Optional.of(buffer.toString());
    }

    @Override
    public Result<Node, ParseException> parse(String input) {
        return parse0(input)
                .<Result<Node, ParseException>>map(Ok::new)
                .orElseGet(() -> new Err<Node, ParseException>(new ParseException("Unknown input", input)));
    }

    @Override
    public Result<String, GenerateException> generate(Node node) {
        return generate0(node)
                .<Result<String, GenerateException>>map(Ok::new)
                .orElseGet(() -> new Err<>(new GenerateException("Unknown node", node)));
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