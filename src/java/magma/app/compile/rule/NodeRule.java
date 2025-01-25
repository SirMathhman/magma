package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.node.MapNode;
import magma.app.compile.node.Node;
import magma.app.compile.node.NodeProperties;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
import magma.java.JavaOptions;

public record NodeRule(String propertyKey, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return this.childRule.parse(input)
                .mapValue(node -> {
                    Node node1 = new MapNode();
                    NodeProperties<Node> nodeNodeProperties = node1.nodes();
                    return nodeNodeProperties.with(this.propertyKey, node).orElse(new MapNode());
                });
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return JavaOptions.toNative(node.nodes().find(this.propertyKey))
                .map(this.childRule::generate)
                .orElseGet(() -> new Err<>(new CompileError("Node '" + this.propertyKey + "' was not present", new NodeContext(node))));
    }
}
