package magma.error;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CompileError implements Error {
    private final String message;
    private final String context;
    private final List<CompileError> children;

    public CompileError(String message, String context) {
        this(message, context, Collections.emptyList());
    }

    public CompileError(String message, String context, List<CompileError> children) {
        this.message = message;
        this.context = context;
        this.children = children;
    }

    public CompileError(String message, String context, CompileError... errors) {
        this(message, context, List.of(errors));
    }

    @Override
    public String display() {
        final var joined = children.stream()
                .map(CompileError::display)
                .map(value -> "\n" + value)
                .collect(Collectors.joining());

        return message + ": " + context + joined;
    }
}
