package magma.app.compile.rule;

import magma.api.collect.List;
import magma.api.java.MutableJavaList;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;
import magma.app.error.FormattedError;
import magma.app.error.InputContext;

public class BracketDivider implements Divider {
    static State processChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';' && appended.isLevel()) {
            return appended.advance();
        } else if (c == '}' && appended.isShallow()) {
            return appended.advance().exit();
        } else {
            if (c == '{') return appended.enter();
            if (c == '}') return appended.exit();
            return appended;
        }
    }

    @Override
    public Result<List<Input>, FormattedError> divide(Input input) {
        var state = new State();
        for (int i = 0; i < input.getInput().length(); i++) {
            var c = input.getInput().charAt(i);
            state = processChar(state, c);
        }

        if (state.isLevel()) {
            return new Ok<>(state.advance().segments);
        } else {
            return new Err<>(new CompileError("Invalid depth of '" + state.depth + "'", new InputContext(input)));
        }
    }

    @Override
    public StringBuilder concat(StringBuilder buffer, String slice) {
        return buffer.append(slice);
    }

    record State(
            List<Input> segments,
            StringBuilder buffer,
            int depth
    ) {
        public State() {
            this(new MutableJavaList<>(), new StringBuilder(), 0);
        }

        private boolean isLevel() {
            return depth == 0;
        }

        private State append(char c) {
            return new State(segments, buffer.append(c), depth);
        }

        State advance() {
            if (buffer().isEmpty()) return this;
            return new State(segments.add(new Input(buffer.toString())), new StringBuilder(), 0);
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