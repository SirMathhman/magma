package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;
import magma.api.result.Err;
import magma.api.result.Result;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (!input.endsWith(suffix())) {
            final var format = "Suffix '%s' not present";
            final var message = format.formatted(suffix());
            final var error = new CompileError(message, new StringContext(input));
            return new Err<>(error);
        }

        final var withoutSuffix = input.substring(0, input.length() - suffix.length());
        return childRule().parse(withoutSuffix);
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.generate(node).mapValue(value -> value + suffix);
    }
}