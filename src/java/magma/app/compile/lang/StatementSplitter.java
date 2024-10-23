package magma.app.compile.lang;

import magma.app.compile.rule.Splitter;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StatementSplitter implements Splitter {
    static State splitAtChar(State state, char c, Deque<Character> queue) {
        final var appended = state.append(c);
        if (c == '\'') {
            final var next = queue.pop();
            final State escaped;
            if (next == '\\') {
                final var escapedValue = queue.pop();
                escaped = appended.append('\\').append(escapedValue);
            } else {
                escaped = appended;
            }

            final var closing = queue.pop();
            return escaped.append(closing);
        }

        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{' || c == '(') return appended.enter();
        if (c == '}' || c == ')') return appended.exit();
        return appended;
    }

    @Override
    public List<String> split(String input) {
        final var length = input.length();
        var state = new State();

        final var queue = IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            state = splitAtChar(state, c, queue);
        }

        return state.advance().segments;
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