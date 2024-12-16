package magma;

import magma.error.CompileError;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

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
