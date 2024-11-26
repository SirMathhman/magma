package magma.error;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public record CompileError(String message, String context, Option<CompileError> maybeCause) implements Error {
    public CompileError(String message, String context) {
        this(message, context, new None<>());
    }

    public CompileError(String message, String content, CompileError error) {
        this(message, content, new Some<>(error));
    }

    @Override
    public String display() {
        return format(0);
    }

    private String format(int depth) {
        final var causeString = maybeCause.map(cause -> "\n" + cause.format(depth + 1)).orElse("");
        return "\t".repeat(depth) + message + ": " + context + causeString;
    }
}
