package magma.rule;

import magma.Node;
import magma.Rule;
import magma.error.CompileError;
import magma.error.StringContext;
import magma.result.Err;
import magma.result.Result;

public record SuffixRule(Rule childRule, String slice) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String value) {
        if (!value.endsWith(slice())) {
            final var context = new StringContext(value);
            final var message = "Suffix '%s' not present".formatted(slice());
            return new Err<>(new CompileError(message, context));
        }

        final var content = value.substring(0, value.length() - slice().length());
        return childRule.parse(content);
    }
}