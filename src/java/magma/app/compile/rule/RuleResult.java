package magma.app.compile.rule;

import magma.api.result.Result;
import magma.app.compile.CompileException;

import java.util.List;
import java.util.Optional;

public record RuleResult<T, E extends CompileException>(List<Result<T, E>> results) {
    public Optional<Result<T, E>> first() {
        return results.stream().findFirst();
    }
}
