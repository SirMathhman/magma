package magma.app.compile.lang;

import magma.app.compile.rule.Splitter;

import java.util.ArrayList;
import java.util.List;

public class StatementSplitter implements Splitter {
    static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    @Override
    public List<String> split(String input) {
        final var length = input.length();
        var state = new State();

        for (int i = 0; i < length; i++) {
            final var c = input.charAt(i);
            state = splitAtChar(state, c);
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