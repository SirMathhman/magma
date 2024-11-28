package magma.rule;

import magma.Node;
import magma.error.CompileError;
import magma.error.NodeContext;
import magma.result.Err;
import magma.result.Result;

public record NodeRule(String propertyKey, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input).mapValue(value -> new Node().withNode(propertyKey, value));
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

        final var value = option.orElse(new Node());
        return childRule.generate(value);
    }
}
