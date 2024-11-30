package magma.app.compile.error;

import magma.api.error.Error;
import magma.java.JavaList;

public record CompileError(String message, Context context, JavaList<Error> causes) implements Error {
    public CompileError(String message, Context context) {
        this(message, context, new JavaList<>());
    }

    public CompileError() {
        this("", new StringContext(""));
    }

    public CompileError(String message, Context context, Error cause) {
        this(message, context, new JavaList<Error>().add(cause));
    }

    @Override
    public String display() {
        return format(0);
    }

    @Override
    public String format(int depth) {
        final var causesString = causes.stream()
                .map(error -> error.format(depth + 1))
                .map(value -> "\n" + "\t".repeat(depth + 1) + value)
                .foldLeft((s, s2) -> s + s2)
                .orElse("");

        return depth + ") " + message + ": " + context.display() + causesString;
    }
}
