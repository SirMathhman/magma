package magma.app.rule;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.app.error.NodeContext;
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
        return node.findString(propertyKey)
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + propertyKey + "' not present", new NodeContext(node))));
    }
}
