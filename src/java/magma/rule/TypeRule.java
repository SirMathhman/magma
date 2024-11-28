package magma.rule;

import magma.Node;
import magma.error.NodeContext;
import magma.error.CompileError;
import magma.result.Err;
import magma.result.Result;

public record TypeRule(String type, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input).mapValue(value -> value.retype(type));
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
