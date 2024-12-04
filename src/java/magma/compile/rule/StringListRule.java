package magma.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.NodeContext;
import magma.java.JavaList;

import java.util.List;

public record StringListRule(String propertyKey, String delimiterRegex) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        final var segments = new JavaList<>(List.of(input.split(delimiterRegex)));
        return new Ok<>(new Node().withStringList(propertyKey, segments));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return node.findStringList(propertyKey)
                .<Result<String, CompileError>>map(list -> new Ok<>(String.join(delimiterRegex, list.list())))
                .orElseGet(() -> createGenerationError(node));
    }

    private Result<String, CompileError> createGenerationError(Node node) {
        final var format = "String list '%s' not present";
        final var message = format.formatted(propertyKey);
        final var context = new NodeContext(node);
        return new Err<>(new CompileError(message, context));
    }
}
