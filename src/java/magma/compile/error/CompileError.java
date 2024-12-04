package magma.compile.error;

import magma.api.error.Error;
import magma.java.JavaList;

import java.util.Collections;

public class CompileError implements Error {
    private final String message;
    private final Context context;
    private final JavaList<CompileError> children;

    public CompileError(String message, Context context) {
        this(message, context, new JavaList<>());
    }

    public CompileError(String message, Context context, JavaList<CompileError> children) {
        this.message = message;
        this.context = context;
        this.children = children;
    }

    public CompileError(String message, StringContext context, CompileError cause) {
        this(message, context, new JavaList<>(Collections.singletonList(cause)));
    }

    @Override
    public String display() {
        return format(0);
    }

    private String format(int depth) {
        final var joinedChildren = children.stream()
                .map(child -> child.format(depth + 1))
                .map(child -> "\n" + child)
                .foldLeft((previous, next) -> previous + next)
                .orElse("");

        return "\t".repeat(depth) + depth + ") " + message + ": " + context.display() + joinedChildren;
    }
}
