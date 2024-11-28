package magma.rule;

import magma.Node;
import magma.error.CompileError;
import magma.error.NodeContext;
import magma.error.StringContext;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

public record IntRule(String propertyKey) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        try {
            final var value = Integer.parseInt(input);
            return new Ok<>(new Node().withInt(propertyKey, value));
        } catch (NumberFormatException e) {
            return new Err<>(new CompileError("Not an int", new StringContext(input)));
        }
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var option = node.findInt(propertyKey);
        if (option.isEmpty())
            return new Err<>(new CompileError("Int '" + propertyKey + "' not present", new NodeContext(node)));
        final var value = option.orElse(0);
        return new Ok<>(String.valueOf(value));
    }
}
