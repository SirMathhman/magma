package magma;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record CompileError(String message, String context, List<CompileError> errors) implements Error {
    public CompileError(String message, String context) {
        this(message, context, Collections.emptyList());
    }

    @Override
    public String display() {
        return format(0, 0);
    }

    private int computeDepth() {
        return 1 + this.errors.stream()
                .mapToInt(CompileError::computeDepth)
                .max()
                .orElse(0);
    }

    private String format(int index, int depth) {
        this.errors.sort(Comparator.comparingInt(CompileError::computeDepth));

        final var joined = IntStream.range(0, this.errors.size())
                .mapToObj(inner -> this.errors.get(inner).format(inner, depth + 1))
                .collect(Collectors.joining());

        return "\n" + "\t".repeat(depth) + index + ") " + this.message + ": " + this.context + joined;
    }
}
