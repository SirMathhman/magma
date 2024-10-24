package magma.app.compile.lang;

import magma.app.compile.rule.Splitter;
import magma.java.JavaCollectors;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StatementSplitter implements Splitter {
    static BufferedState splitAtChar(BufferedState state, char c) {
        final var appended = state.append(c);
        return splitDoubleQuotes(appended, c)
                .or(() -> splitSingleQuotes(appended, c))
                .orElseGet(() -> splitOther(c, appended));
    }

    private static Optional<BufferedState> splitDoubleQuotes(BufferedState state, char c) {
        if (c != '\"') return Optional.empty();

        var current = state;
        while (true) {
            final var optional = state.popAndAppend();
            if (optional.isEmpty()) break;

            final var next = optional.get();
            final var nextState = next.left();
            final var nextChar = next.right();
            if (nextChar == '\\') {
                current = nextState.popAndAppendDiscard().orElse(nextState);
            } else if (nextChar == '\"') {
                break;
            } else {
                current = nextState;
            }
        }

        return Optional.of(current);
    }

    private static Optional<BufferedState> splitSingleQuotes(BufferedState state, char c) {
        if (c != '\'') return Optional.empty();

        final var optional = state.popAndAppend();
        if(optional.isEmpty()) return Optional.of(state);

        final var next = optional.get();
        final var nextState = next.left();
        final var nextChar = next.right();

        final BufferedState escaped;
        if (nextChar == '\\') {
            escaped = nextState.popAndAppendDiscard().orElse(nextState);
        } else {
            escaped = nextState;
        }

        return escaped.popAndAppendDiscard();
    }

    private static BufferedState splitOther(char c, BufferedState appended) {
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{' || c == '(') return appended.enter();
        if (c == '}' || c == ')') return appended.exit();
        return appended;
    }

    @Override
    public List<String> split(String input) {
        final var queue = IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        var state = new BufferedState(queue);
        while (true) {
            final var optional = state.popAndAppend();
            if (optional.isEmpty()) break;

            final var next = optional.get();
            state = splitAtChar(next.left(), next.right());
        }

        return state.advance().stream().collect(JavaCollectors.asList());
    }

    static class State {
        private final List<String> segments;
        private final StringBuilder buffer;
        private final int depth;

        private State() {
            this(new ArrayList<>(), new StringBuilder(), 0);
        }

        private State(List<String> segments, StringBuilder buffer, int depth) {
            this.buffer = buffer;
            this.segments = segments;
            this.depth = depth;
        }

        private State append(char c) {
            return new State(segments, buffer.append(c), depth);
        }

        private State advance() {
            if (buffer.toString().trim().isEmpty()) return this;

            final var copy = new ArrayList<>(segments);
            copy.add(buffer.toString());
            return new State(copy, new StringBuilder(), depth);
        }

        public boolean isLevel() {
            return depth == 0;
        }

        public State enter() {
            return new State(segments, buffer, depth + 1);
        }

        public State exit() {
            return new State(segments, buffer, depth - 1);
        }

        public boolean isShallow() {
            return depth == 1;
        }
    }
}