package magma.rule;

import magma.Node;
import magma.error.CompileError;
import magma.result.Err;
import magma.result.Result;

public record PrefixRule(String prefix, SuffixRule rule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (!input.startsWith(prefix())) {
            final var format = "Prefix '%s' not present";
            final var message = format.formatted(prefix());
            final var error = new CompileError(message, input);
            return new Err<>(error);
        }

        return rule().parse(input.substring(prefix().length()));
    }
}