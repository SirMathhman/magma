package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;

public record StringRule(String propertyKey) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return new Ok<>(new MapNode().withString(propertyKey, input));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return node.findString(propertyKey)
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + propertyKey + "' does not exist", new NodeContext(node))));
    }
}