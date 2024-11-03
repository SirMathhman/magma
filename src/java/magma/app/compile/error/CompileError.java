package magma.app.compile.error;

import magma.app.Error;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record CompileError(String message, Context context, List<CompileError> errors) implements Error {
    public CompileError(String message, Context context) {
        this(message, context, Collections.emptyList());
    }

    @Override
    public String format(int depth) {
        final var joined = errors.isEmpty() ? "" : "\n" + joinChildren(depth);

        final var prefix = "\t".repeat(depth) + depth + ") ";
        return prefix + message + ": " + context.asString() + joined;
    }

    private String joinChildren(int depth) {
        return errors.stream()
                .map(error -> error.format(depth + 1))
                .collect(Collectors.joining(""));
    }
}
