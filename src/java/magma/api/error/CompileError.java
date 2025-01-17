package magma.api.error;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        return format(0);
    }

    private String format(int depth) {
        final var joinedCauses = IntStream.range(0, this.causes.size())
                .mapToObj(index -> "\n" + index + ") " + this.causes.get(index).format(depth + 1))
                .collect(Collectors.joining());

        return "\t".repeat(depth) + this.message + ": " + this.context + joinedCauses;
    }
}
