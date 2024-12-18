package magma.app.error;

import magma.api.collect.List;
import magma.api.java.MutableJavaList;

import java.util.ArrayList;
import java.util.Comparator;

public final class CompileError implements FormattedError {
    private final Detail detail;
    private final List<FormattedError> causes;

    public CompileError(Detail detail, List<FormattedError> causes) {
        this.detail = detail;
        this.causes = causes;
    }

    public CompileError(Detail detail, FormattedError... errors) {
        this(detail, new MutableJavaList<>(new ArrayList<>(java.util.List.of(errors))));
    }

    @Override
    public String display() {
        return format(0);
    }

    @Override
    public int computeMaxDepth() {
        return 1 + causes.stream()
                .map(FormattedError::computeMaxDepth)
                .foldLeft(0, Integer::sum);
    }

    @Override
    public String format(int depth) {
        final var joinedCauses = causes.sort(Comparator.comparingInt(FormattedError::computeMaxDepth))
                .stream()
                .map(cause -> cause.format(depth + 1))
                .foldLeft("", (previous, next) -> previous + "\n" + next);

        return "\t".repeat(depth) + depth + ") " + detail.display() + joinedCauses;
    }
}
