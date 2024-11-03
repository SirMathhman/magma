package magma.compile.rule;

import magma.compile.error.CompileError;
import magma.compile.MapNode;
import magma.compile.Node;
import magma.compile.error.StringContext;
import magma.compile.error.NodeContext;
import magma.option.Option;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.util.Map;

public record ExtractRule(String propertyKey) implements Rule {
    private Option<Node> parse0(String input) {
        return new Some<>(new MapNode(Map.of(propertyKey(), input)));
    }

    private Option<String> generate0(Node node) {
        return node.findString(propertyKey);
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return parse0(input)
                .<Result<Node, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("Invalid input", new StringContext(input))));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return generate0(node)
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("Invalid node", new NodeContext(node))));
    }
}