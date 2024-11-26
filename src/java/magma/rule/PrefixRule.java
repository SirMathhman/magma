package magma.rule;

import magma.Node;
import magma.Rule;
import magma.error.CompileError;
import magma.error.StringContext;
import magma.result.Err;
import magma.result.Result;

public record PrefixRule(String slice, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String value) {
        if (!value.startsWith(slice)) {
            final var context = new StringContext(value);
            final var message = "Prefix '%s' not present".formatted(slice);
            return new Err<>(new CompileError(message, context));
        }

        final var content = value.substring(slice.length());
        return childRule.parse(content);
    }
}