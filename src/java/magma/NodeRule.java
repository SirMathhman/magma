package magma;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.NodeContext;
import magma.compile.rule.Rule;

public record NodeRule(String propertyKey, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input).mapValue(node -> new Node().withNode(propertyKey, node));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return node.findNode(propertyKey)
                .map(childRule::generate)
                .orElseGet(() -> new Err<>(new CompileError("Node '" + propertyKey + "' not present", new NodeContext(node))));
    }
}
