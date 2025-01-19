package magma.app.rule;

import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;

import java.util.function.Function;

public record StripRule(
        Function<String, Result<Node, CompileError>> childRule) implements Function<String, Result<Node, CompileError>> {
    @Override
    public Result<Node, CompileError> apply(String input) {
        return this.childRule.apply(input.strip());
    }
}
