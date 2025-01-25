package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Input;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.StringInput;
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
        return new Ok<>(node.inputs().with(this.propertyKey, new StringInput(this.propertyKey)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return JavaOptions.toNative(node.inputs().find(this.propertyKey).map(Input::unwrap))
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + this.propertyKey + "' not present", new NodeContext(node))));
    }
}
