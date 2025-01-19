package magma.error;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record CompileError(String message, String context, List<CompileError> children) implements Error {
    public CompileError(String message, String context) {
        this(message, context, Collections.emptyList());
    }

    @Override
    public String display() {
        final var joinedChildren = this.children.stream()
                .map(CompileError::display)
                .collect(Collectors.joining());

        return this.message + ": " + this.context + joinedChildren;
    }
}
