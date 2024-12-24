package magma.compile.error;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CompileError implements Error {
    private final String message;
    private final Context context;
    private final List<CompileError> errors;

    public CompileError(String message, Context context) {
        this(message, context, Collections.emptyList());
    }

    public CompileError(String message, Context context, List<CompileError> errors) {
        this.message = message;
        this.context = context;
        this.errors = errors;
    }

    @Override
    public String display() {
        return format(0);
    }

    private String format(int depth) {
        errors.sort(Comparator.comparing(CompileError::computeMaxDepth));

        final var joinedErrors = errors.stream()
                .map(compileError -> compileError.format(depth + 1))
                .map(value -> "\n" + value)
                .collect(Collectors.joining());

        return " ".repeat(depth) + depth + ") " + message + ": " + context.display() + joinedErrors;
    }

    private int computeMaxDepth() {
        return 1 + errors.stream()
                .mapToInt(CompileError::computeMaxDepth)
                .max()
                .orElse(0);
    }
}
