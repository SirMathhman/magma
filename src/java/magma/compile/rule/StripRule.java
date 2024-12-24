package magma.compile.rule;

import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;

public record StripRule(String beforeKey, Rule childRule, String afterKey) implements Rule {
    public StripRule(Rule childRule) {
        this("", childRule, "");
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input.strip());
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var before = node.findString(beforeKey).orElseGet(() -> "");
        final var after = node.findString(afterKey).orElseGet(() -> "");
        return childRule.generate(node).mapValue(value -> before + value + after);
    }
}
