package magma.compile.rule;

import magma.compile.CompileError;
import magma.compile.MapNode;
import magma.compile.Node;
import magma.core.String_;
import magma.core.result.Err;
import magma.core.result.Ok;
import magma.core.result.Result;

public record ExtractRule(String propertyKey) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String_ input) {
        final var node = new MapNode()
                .withString(propertyKey, input)
                .orElse(new MapNode());

        return new Ok<>(node);
    }

    @Override
    public Result<String_, CompileError> generate(Node node) {
        return node.find(propertyKey).<Result<String_, CompileError>>map(Ok::new).orElseGet(() -> {
            final var format = "String '%s' not present";
            final var message = format.formatted(propertyKey);
            final var error = CompileError.create(message, node);
            return new Err<>(error);
        });
    }
}