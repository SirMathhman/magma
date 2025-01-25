package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
import magma.java.JavaOptions;

public record NodeRule(String propertyKey, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return this.childRule.parse(input)
                .mapValue(node -> {
                    Node node1 = new MapNode();
                    return node1.nodes().with(this.propertyKey, node);
                });
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return JavaOptions.toNative(node.nodes().find(this.propertyKey))
                .map(this.childRule::generate)
                .orElseGet(() -> new Err<>(new CompileError("Node '" + this.propertyKey + "' was not present", new NodeContext(node))));
    }
}
