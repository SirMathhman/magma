package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.error.StringContext;

public record IntRule(String propertyKey) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        try {
            return new Ok<>(new MapNode().withInt(propertyKey, Integer.parseInt(input)));
        } catch (NumberFormatException e) {
            return new Err<>(new CompileError("Not an integer", new StringContext(input)));
        }
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var option = node.findInt(propertyKey);
        if (option.isEmpty())
            return new Err<>(new CompileError("Integer '" + propertyKey + "' not present", new NodeContext(node)));

        return new Ok<>(String.valueOf(option.orElse(0)));
    }
}
