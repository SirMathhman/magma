package magma.app.compile.rule;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.CompileError;
import magma.app.error.NodeContext;
import magma.app.error.StringContext;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

public record IntRule(String propertyKey) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        try {
            final var value = Integer.parseInt(input);
            return new Ok<>(new MapNode().withInt(propertyKey, value));
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
