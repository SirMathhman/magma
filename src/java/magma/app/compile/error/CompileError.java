package magma.app.compile.error;

import magma.app.Error;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public record CompileError(String message, Context context, List<CompileError> errors) implements Error {
    public CompileError(String message, Context context) {
        this(message, context, Collections.emptyList());
    }

    @Override
    public String format(int depth, int index) {
        final var joined = errors.isEmpty() ? "" : "\n" + joinChildren(depth);

        final var prefix = "\t".repeat(depth) + (index + 1) + ") ";
        return prefix + message + ": " + context.asString() + joined;
    }

    private String joinChildren(int depth) {
        var joiner = new StringJoiner("\n");
        for (int index = 0; index < errors.size(); index++) {
            CompileError error = errors.get(index);
            String format = error.format(depth + 1, index);
            joiner.add(format);
        }

        return joiner.toString();
    }
}
