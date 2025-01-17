package magma.api.compile.rule;

import magma.api.compile.Node;
import magma.api.error.CompileError;
import magma.api.result.Result;

public record StripRule(Rule child) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return child().parse(input.strip());
    }
}