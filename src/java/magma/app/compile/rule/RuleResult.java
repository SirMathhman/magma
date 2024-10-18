package magma.app.compile.rule;

import magma.api.result.Result;

public record RuleResult<T, E>(Result<T, E> result) {
    @Deprecated
    public Result<T, E> unwrap() {
        return result;
    }
}
