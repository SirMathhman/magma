package magma.api.compile.rule;

import magma.api.compile.Node;
import magma.api.error.CompileError;
import magma.api.result.Ok;
import magma.api.result.Result;

public record StringRule(String propertyKey) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return new Ok<>(new Node().withString(propertyKey(), input));
    }
}