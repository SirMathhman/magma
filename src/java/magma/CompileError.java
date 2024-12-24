package magma;

import java.util.Collections;
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
        final var joinedErrors = errors.stream()
                .map(Error::display)
                .collect(Collectors.joining());

        return message + ": " + context.display() + joinedErrors;
    }
}
