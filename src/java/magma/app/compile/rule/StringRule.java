package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.node.Input;
import magma.app.compile.node.MapNode;
import magma.app.compile.node.Node;
import magma.app.compile.node.NodeProperties;
import magma.app.compile.node.StringInput;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
import magma.java.JavaOptions;

public class StringRule implements Rule {
    private final String propertyKey;

    public StringRule(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public Result<String, CompileError> parse(Node node) {
        return JavaOptions.toNative(node.inputs().find(this.propertyKey).map(Input::unwrap))
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + this.propertyKey + "' not present", new NodeContext(node))));
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        Node node = new MapNode();
        NodeProperties<Input> inputNodeProperties = node.inputs();
        Input propertyValue = new StringInput(this.propertyKey);
        return new Ok<>(inputNodeProperties.with(this.propertyKey, propertyValue).orElse(new MapNode()));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return JavaOptions.toNative(node.inputs().find(this.propertyKey).map(Input::unwrap))
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + this.propertyKey + "' not present", new NodeContext(node))));
    }
}
