package magma.app.compile.rule;

import magma.api.result.Result;

import java.util.Collections;
import java.util.List;

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
}
