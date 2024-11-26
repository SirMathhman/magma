package magma.rule;

import magma.Node;
import magma.Rule;
import magma.error.CompileError;
import magma.result.Ok;
import magma.result.Result;

public record StringRule(String propertyKey) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String value) {
        return new Ok<>(new Node().withString(propertyKey, value));
    }
}
