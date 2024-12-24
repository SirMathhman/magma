package magma.compile.rule;

import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;

public record StripRule(Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input.strip());
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.generate(node);
    }
}
