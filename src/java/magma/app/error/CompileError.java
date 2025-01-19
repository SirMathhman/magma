package magma.app.error;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record CompileError(String message, String context, List<CompileError> children) implements Error {
    public CompileError(String message, String context) {
        this(message, context, Collections.emptyList());
    }

    @Override
    public String display() {
        return format(0);
    }

    public int maxDepth() {
        return 1 + this.children.stream()
                .mapToInt(CompileError::maxDepth)
                .max()
                .orElse(0);
    }

    private String format(int depth) {
        this.children.sort(Comparator.comparingInt(CompileError::maxDepth));

        final var joinedChildren = IntStream.range(0, this.children.size())
                .mapToObj(index -> "\n" + "\t".repeat(depth) + index + ") " + this.children.get(index).format(depth + 1))
                .collect(Collectors.joining());

        return this.message + ": " + this.context + joinedChildren;
    }
}
