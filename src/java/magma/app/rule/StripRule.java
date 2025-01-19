package magma.app.rule;

import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;

public record StripRule(
        Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return this.childRule.parse(input.strip());
    }
}
