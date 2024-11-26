package magma.error;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public record CompileError(String message, Context context, Option<CompileError> maybeCause) implements Error {
    public CompileError(String message, Context context) {
        this(message, context, new None<>());
    }

    public CompileError(String message, Context context, CompileError error) {
        this(message, context, new Some<>(error));
    }

    @Override
    public String display() {
        return format(0);
    }

    private String format(int depth) {
        final var causeString = maybeCause.map(cause -> "\n" + cause.format(depth + 1)).orElse("");
        return "\t".repeat(depth) + message + ": " + context.format() + causeString;
    }
}
