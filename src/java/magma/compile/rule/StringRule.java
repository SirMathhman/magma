package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.NodeContext;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

public class StringRule implements Rule {
    private final String propertyKey;

    public StringRule(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return new Ok<>(new Node().withString(propertyKey, input));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return node.findString(propertyKey)
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("Unknown property key", new NodeContext(node))));
    }
}