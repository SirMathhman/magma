package magma.app.compile.rule;

import magma.app.compile.Node;
import magma.app.compile.CompileError;
import magma.api.result.Result;

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
