package magma;

import magma.error.CompileError;
import magma.result.Result;

public record StripRule(Rule child) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return child().parse(input.strip());
    }
}