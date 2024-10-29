package magma.compile.rule;

import magma.compile.CompileError;
import magma.compile.Node;
import magma.core.String_;
import magma.core.result.Err;
import magma.core.result.Result;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String_ input) {
        return input.truncateRightBySlice(suffix)
                .map(childRule::parse)
                .orElseGet(() -> {
                    final var format = "Suffix '%s' not present";
                    final var message = format.formatted(suffix);
                    final var error = CompileError.create(message, input);
                    return new Err<>(error);
                });
    }

    @Override
    public Result<String_, CompileError> generate(Node node) {
        return childRule.generate(node).mapValue(inner -> inner.appendSlice(suffix));
    }
}