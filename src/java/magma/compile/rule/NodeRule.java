package magma.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.NodeContext;

public record NodeRule(String propertyKey, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input).mapValue(inner -> new Node().withNode(propertyKey, inner));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return node.findNode(propertyKey)
                .map(childRule::generate)
                .orElseGet(() -> createError(node));
    }

    private Result<String, CompileError> createError(Node node) {
        final var format = "Node '%s' does not exist";
        final var message = format.formatted(propertyKey);
        final var context = new NodeContext(node);
        return new Err<>(new CompileError(message, context));
    }
}
