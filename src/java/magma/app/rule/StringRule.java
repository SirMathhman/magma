package magma.app.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;

import java.util.function.Function;

public class StringRule implements Function<Node, Result<String, CompileError>> {
    private final String propertyKey;

    public StringRule(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    @Override
    public Result<String, CompileError> apply(Node node) {
        return node.findString(this.propertyKey)
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + this.propertyKey + "' not present", new NodeContext(node))));
    }
}
