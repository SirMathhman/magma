package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;

public record NodeRule(String propertyKey, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input).mapValue(node -> new MapNode().withNode(propertyKey, node));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return node.findNode(propertyKey)
                .map(childRule::generate)
                .orElseGet(() -> new Err<>(new CompileError("Node '" + propertyKey + "' does not exist", new NodeContext(node))));
    }
}
