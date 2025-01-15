package magma.error;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public String display() {
        final var joinedCauses = this.causes.stream()
                .map(CompileError::display)
                .collect(Collectors.joining());

        return this.message + ": " + this.context + joinedCauses;
    }
}
