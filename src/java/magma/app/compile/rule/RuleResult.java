package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.singletonList;

public record RuleResult<T, E>(Result<T, E> result, List<RuleResult<T, E>> children) {
    public RuleResult(Result<T, E> result) {
        this(result, Collections.emptyList());
    }

    @Deprecated
    public Result<T, E> unwrap() {
        return result;
    }

    public boolean isValid() {
        return result.isOk();
    }

    public RuleResult<T, E> mapValue(Function<T, T> mapper) {
        return new RuleResult<>(result.mapValue(mapper), children);
    }

    public RuleResult<T, E> wrapErr(E error) {
        if (!isError()) return this;

        var list = singletonList(this);
        return new RuleResult<>(new Err<>(error), list);
    }

    public boolean isError() {
        return result.isErr();
    }

    public int depth() {
        return 1 + children.stream()
                .map(RuleResult::depth)
                .max(Integer::compare)
                .orElse(0);
    }

    public List<RuleResult<T, E>> sortedChildren() {
        return children.stream()
                .sorted(Comparator.comparingInt(RuleResult::depth))
                .toList();
    }
}
