package magma.app.error;

import magma.api.collect.List;
import magma.api.java.MutableJavaList;

import java.util.ArrayList;
import java.util.Comparator;

public record CompileError(String message, Context context, List<FormattedError> causes) implements FormattedError {
    public CompileError(String message, Context context) {
        this(message, context, new MutableJavaList<>());
    }

    public CompileError(String message, Context context, FormattedError... errors) {
        this(message, context, new MutableJavaList<>(new ArrayList<>(java.util.List.of(errors))));
    }

    @Override
    public String display() {
        return format(0);
    }

    @Override
    public int computeMaxDepth() {
        return 1 + causes.stream()
                .map(FormattedError::computeMaxDepth)
                .foldLeft(0, Integer::sum);
    }

    @Override
    public String format(int depth) {
        final var joinedCauses = causes.sort(Comparator.comparingInt(FormattedError::computeMaxDepth))
                .stream()
                .map(cause -> cause.format(depth + 1))
                .foldLeft("", (previous, next) -> previous + "\n" + next);

        return "\t".repeat(depth) + depth + ") " + message + ": " + context.display() + joinedCauses;
    }
}
