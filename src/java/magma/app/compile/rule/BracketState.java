package magma.app.compile.rule;

import magma.api.Tuple;
import magma.api.collect.List;
import magma.api.java.MutableJavaList;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.*;

import java.util.Deque;

record BracketState(
        Deque<Character> queue, List<Input> segments,
        StringBuilder buffer,
        int depth
) {

    public BracketState(Deque<Character> queue) {
        this(queue, new MutableJavaList<>(), new StringBuilder(), 0);
    }

    Result<List<Input>, FormattedError> complete(Input input) {
        if (isLevel()) {
            return new Ok<>(advance().segments());
        } else {
            final var format = "Invalid depth of '%d'";
            final var message = format.formatted(depth());
            final var context = new InputContext(input);
            final var detail = new ContextDetail(message, context);
            return new Err<>(new CompileError(detail));
        }
    }

    public boolean isLevel() {
        return depth == 0;
    }

    public BracketState append(char c) {
        return new BracketState(queue, segments, buffer.append(c), depth);
    }

    BracketState advance() {
        if (buffer().isEmpty()) return this;
        return new BracketState(queue, segments.add(new Input(buffer.toString())), new StringBuilder(), depth);
    }

    public BracketState enter() {
        return new BracketState(queue, segments, buffer, depth + 1);
    }

    public Result<BracketState, FormattedError> exit() {
        if (depth == 0) {
            return new Err<>(new CompileError(new SimpleDetail("Cannot exit 0 depth: " + this)));
        }
        return new Ok<>(new BracketState(queue, segments, buffer, depth - 1));
    }

    @Override
    public String toString() {
        return "BracketState{" +
                "segments=" + segments +
                ", buffer=" + buffer +
                ", depth=" + depth +
                '}';
    }

    public boolean isShallow() {
        return depth == 1;
    }

    public boolean hasMore() {
        return !queue.isEmpty();
    }

    public Option<Tuple<BracketState, Character>> appendAndPop() {
        if (!hasMore()) return new None<>();

        final var c = queue.pop();
        final var appended = append(c);
        return new Some<>(new Tuple<>(appended, c));
    }
}
