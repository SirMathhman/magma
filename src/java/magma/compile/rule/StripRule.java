package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.api.result.Result;

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