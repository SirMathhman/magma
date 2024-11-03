package magma.compile.rule;

import magma.compile.MapNode;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.NodeContext;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.util.Map;

public record ExtractRule(String propertyKey) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return new Ok<>(new MapNode(Map.of(this.propertyKey(), input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return node.findString(propertyKey)
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + propertyKey + "' does not exist", new NodeContext(node))));
    }
}