package magma.app.compile.rule;

import magma.api.collect.List;
import magma.api.java.MutableJavaList;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.*;

public class BracketDivider implements Divider {
    static Result<State, FormattedError> processChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';' && appended.isLevel()) {
            return new Ok<>(appended.advance());
        } else if (c == '}' && appended.isShallow()) {
            return appended.advance().exit();
        } else {
            if (c == '{') return new Ok<>(appended.enter());
            if (c == '}') return appended.exit();
            return new Ok<>(appended);
        }
    }

    @Override
    public Result<List<Input>, FormattedError> divide(Input input) {
        Result<State, FormattedError> stateResult = new Ok<>(new State());
        for (int i = 0; i < input.getInput().length(); i++) {
            var c = input.getInput().charAt(i);
            stateResult = stateResult.flatMapValue(inner -> processChar(inner, c));
        }

        return stateResult.flatMapValue(state -> {
            if (state.isLevel()) {
                return new Ok<>(state.advance().segments);
            } else {
                final var detail = new ContextDetail("Invalid depth of '" + state.depth + "'", new InputContext(input));
                return new Err<>(new CompileError(detail));
            }
        });
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
            return new State(segments.add(new Input(buffer.toString())), new StringBuilder(), depth);
        }

        public State enter() {
            return new State(segments, buffer, depth + 1);
        }

        public Result<State, FormattedError> exit() {
            if (depth == 0) {
                return new Err<>(new CompileError(new SimpleDetail("Cannot exit 0 depth: " + this)));
            }
            return new Ok<>(new State(segments, buffer, depth - 1));
        }

        public boolean isShallow() {
            return depth == 1;
        }
    }
}