package magma;

import magma.collect.List;
import magma.io.Error;
import magma.java.JavaList;
import magma.stream.Collector;
import magma.stream.Collectors;

public record CompileError(String message, String context, List<CompileError> children) implements Error {
    public CompileError(String message, String context) {
        this(message, context, new JavaList<>());
    }

    public CompileError(String message, String context, CompileError... errors) {
        this(message, context, JavaList.of(errors));
    }

    @Override
    public String display() {
        final var joinedChildren = children.stream()
                .map(child -> child.display())
                .collect(Collectors.joining("\n"))
                .orElse("");

        return this.message + ": " + this.context + joinedChildren;
    }
}
