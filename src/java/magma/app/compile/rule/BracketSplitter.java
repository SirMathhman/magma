package magma.app.compile.rule;

import magma.api.collect.List;
import magma.api.collect.MutableList;

public class BracketSplitter implements Splitter {
    static State processChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';' && appended.isLevel()) {
            return appended.advance();
        } else {
            if (c == '{') return appended.enter();
            if (c == '}') return appended.exit();
            return appended;
        }
    }

    @Override
    public List<String> split(String root) {
        var state = new State();
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            state = processChar(state, c);
        }

        return state.advance().segments;
    }

    @Override
    public StringBuilder merge(StringBuilder buffer, String slice) {
        return buffer.append(slice);
    }

    record State(
            List<String> segments,
            StringBuilder buffer,
            int depth
    ) {
        public State() {
            this(new MutableList<>(), new StringBuilder(), 0);
        }

        private boolean isLevel() {
            return depth == 0;
        }

        private State append(char c) {
            return new State(segments, buffer.append(c), depth);
        }

        State advance() {
            if (buffer().isEmpty()) return this;
            return new State(segments.add(buffer.toString()), new StringBuilder(), 0);
        }

        public State enter() {
            return new State(segments, buffer, depth + 1);
        }

        public State exit() {
            return new State(segments, buffer, depth - 1);
        }
    }
}