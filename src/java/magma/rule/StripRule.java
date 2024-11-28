package magma.rule;

import magma.Node;
import magma.error.CompileError;
import magma.result.Result;

public record StripRule(Rule rule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return rule.parse(input.strip());
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return rule.generate(node);
    }
}
