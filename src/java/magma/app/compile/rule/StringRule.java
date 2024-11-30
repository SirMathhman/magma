package magma.app.compile.rule;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.NodeContext;
import magma.app.compile.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

public record StringRule(String propertyKey) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return new Ok<>(new MapNode().withString(propertyKey, input));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var option = node.findString(propertyKey);
        if(option.isEmpty()) return new Err<>(new CompileError("String '" + propertyKey + "' not present", new NodeContext(node)));

        return new Ok<>(option.orElse(""));
    }
}
