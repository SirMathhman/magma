package magma.compile.error;

import magma.api.error.Error;
import magma.java.JavaList;

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

    @Override
    public String display() {
        return format(0);
    }

    private String format(int depth) {
        final var joinedChildren = children.stream()
                .map(child -> child.format(depth))
                .foldLeft((previous, next) -> previous + "\n" + next)
                .orElse("");

        return message + ": " + context.display() + joinedChildren;
    }
}
