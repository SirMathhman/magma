package magma.app.compile.rule;

import magma.app.compile.Node;
import magma.app.error.NodeContext;
import magma.app.compile.CompileError;
import magma.app.error.StringContext;
import magma.api.result.Err;
import magma.api.result.Result;

public record TypeRule(String type, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input)
                .mapValue(value -> value.retype(type))
                .mapErr(err -> {
                    final var message = "Not a '%s'".formatted(type);
                    final var context = new StringContext(input);
                    return new CompileError(message, context, err);
                });
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        if (node.is(type)) return childRule.generate(node);

        final var format = "Type '%s' not present";
        final var message = format.formatted(type);
        final var context = new NodeContext(node);
        final var error = new CompileError(message, context);
        return new Err<>(error);

    }
}
