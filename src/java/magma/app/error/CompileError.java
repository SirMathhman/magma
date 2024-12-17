package magma.app.error;

import magma.api.collect.List;
import magma.api.java.MutableJavaList;

public record CompileError(String message, Context context, List<FormattedError> causes) implements FormattedError {
    public CompileError(String message, Context context) {
        this(message, context, new MutableJavaList<>());
    }

    @Override
    public String display() {
        return format(0);
    }

    @Override
    public String format(int depth) {
        final var joinedCauses = causes.stream()
                .map(cause -> cause.format(depth + 1))
                .foldLeft("", (previous, next) -> previous + "\n" + next);

        return "\t".repeat(depth) + depth + ") " + message + ": " + context.display() + joinedCauses;
    }
}
