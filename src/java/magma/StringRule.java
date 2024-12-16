package magma;

import magma.error.CompileError;
import magma.result.Ok;
import magma.result.Result;

public record StringRule() implements Rule {
    @Override
    public Result<String, CompileError> generate(Node node) {
        return new Ok<>(node);
    }
}
