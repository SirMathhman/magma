package magma.app.compile.rule;

import magma.api.result.Err;
import magma.app.compile.GenerateException;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

public record NodeRule(String propertyKey, Rule propertyRule) implements Rule {
    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        return propertyRule.parseWithTimeout(input).mapValue(node -> new MapNode().withNode(propertyKey, node));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        final var optional = node.findNode(propertyKey);
        if (optional.isEmpty())
            return new RuleResult<>(new Err<>(new GenerateException("Node '" + propertyKey + "' does not exist.", node)));

        final var node1 = optional.get();
        return propertyRule.generate(node1);
    }
}
