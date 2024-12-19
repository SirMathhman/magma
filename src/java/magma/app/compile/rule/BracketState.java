package magma.app.compile.rule;

import magma.api.collect.List;
import magma.api.java.MutableJavaList;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.*;

record BracketState(
        List<Input> segments,
        StringBuilder buffer,
        int depth
) {
    public BracketState() {
        this(new MutableJavaList<>(), new StringBuilder(), 0);
    }

    Result<List<Input>, FormattedError> complete(Input input) {
        if (isLevel()) {
            return new Ok<>(advance().segments());
        } else {
            final var detail = new ContextDetail("Invalid depth of '" + depth() + "'", new InputContext(input));
            return new Err<>(new CompileError(detail));
        }
    }

    public boolean isLevel() {
        return depth == 0;
    }

    public BracketState append(char c) {
        return new BracketState(segments, buffer.append(c), depth);
    }

    BracketState advance() {
        if (buffer().isEmpty()) return this;
        return new BracketState(segments.add(new Input(buffer.toString())), new StringBuilder(), depth);
    }

    public BracketState enter() {
        return new BracketState(segments, buffer, depth + 1);
    }

    public Result<BracketState, FormattedError> exit() {
        if (depth == 0) {
            return new Err<>(new CompileError(new SimpleDetail("Cannot exit 0 depth: " + this)));
        }
        return new Ok<>(new BracketState(segments, buffer, depth - 1));
    }

    public boolean isShallow() {
        return depth == 1;
    }
}
