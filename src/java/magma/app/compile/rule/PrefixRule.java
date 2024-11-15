package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.StringContext;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (input.startsWith(prefix)) {
            final var slice = input.substring(prefix.length());
            return childRule.parse(slice);
        }

        final var format = "Prefix '%s' not present";
        final var message = format.formatted(prefix);
        final var context = new StringContext(input);
        return new Err<>(new CompileError(message, context));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.generate(node).mapValue(value -> prefix + value);
    }
}