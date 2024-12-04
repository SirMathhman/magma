package magma.rule;

import magma.Node;
import magma.error.CompileError;
import magma.result.Result;

public record StripRule(PrefixRule rule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        final var stripped = input.strip();
        return rule().parse(stripped);
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return rule.generate(node);
    }
}