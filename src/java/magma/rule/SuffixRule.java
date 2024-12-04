package magma.rule;

import magma.Node;
import magma.error.CompileError;
import magma.result.Err;
import magma.result.Result;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (!input.endsWith(suffix())) {
            final var format = "Suffix '%s' not present";
            final var message = format.formatted(suffix());
            final var error = new CompileError(message, input);
            return new Err<>(error);
        }

        final var withoutSuffix = input.substring(0, input.length() - 1);
        return childRule().parse(withoutSuffix);
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.generate(node).mapValue(value -> value + suffix);
    }
}