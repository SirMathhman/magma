package magma;

import java.util.List;

public class CompileError implements Error {
    private final String message;
    private final String context;
    private final List<CompileError> causes;

    public CompileError(String message, String context, CompileError... causes) {
        this(message, context, List.of(causes));
    }

    public CompileError(String message, String context, List<CompileError> causes) {
        this.message = message;
        this.context = context;
        this.causes = causes;
    }
}
