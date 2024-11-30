package magma.app.compile.rule;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.api.result.Err;
import magma.api.result.Result;

public record NodeRule(String propertyKey, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input).mapValue(value -> new MapNode().withNode(propertyKey, value));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var option = node.findNode(propertyKey);
        if (option.isEmpty()) {
            final var format = "Node '%s' not present";
            final var message = format.formatted(propertyKey);
            final var context = new NodeContext(node);
            final var error = new CompileError(message, context);
            return new Err<>(error);
        }

        final var value = option.orElse(new MapNode());
        return childRule.generate(value);
    }
}
