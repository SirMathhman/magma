package magma.rule;

import magma.error.CompileError;
import magma.result.Ok;

public record ExactRule(String value) {
    public Ok<String, CompileError> generate() {
        return new Ok<>(value);
    }
}