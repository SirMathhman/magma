package magma.compile.rule;

import magma.compile.MapNode;
import magma.compile.Node;
import magma.compile.CompileError;
import magma.core.String_;
import magma.core.option.Option;
import magma.core.result.Err;
import magma.core.result.Ok;
import magma.core.result.Result;

public record ExtractRule(String propertyKey) implements Rule {
    private Option<Node> parse0(String_ input) {
        return new MapNode().withString(propertyKey(), input);
    }

    private Option<String_> generate0(Node node) {
        return node.find(propertyKey);
    }

    @Override
    public Result<Node, CompileError> parse(String_ input) {
        return parse0(input)
                .<Result<Node, CompileError>>map(node -> new Ok<>(node))
                .orElseGet(() -> new Err<>(CompileError.create("Invalid input", input)));
    }

    @Override
    public Result<String_, CompileError> generate(Node node) {
        return generate0(node)
                .<Result<String_, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(CompileError.create("Invalid node", node)));
    }
}