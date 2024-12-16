package magma;

import magma.error.CompileError;
import magma.result.Ok;
import magma.result.Result;

public record ExactRule(String value) implements Rule {
    @Override
    public Result<String, CompileError> generate() {
        return new Ok<>(value());
    }
}