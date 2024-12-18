package magma.app.compile.rule;

import magma.api.java.MutableJavaList;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.app.error.ContextDetail;
import magma.app.error.FormattedError;
import magma.app.error.NodeContext;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

public record StringRule(String propertyKey) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return new Ok<>(new MapNode().withString(propertyKey, input.input()));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return node.findString(propertyKey)
                .<Result<String, FormattedError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError(new ContextDetail("String '" + propertyKey + "' not present", new NodeContext(node)), new MutableJavaList<>())));
    }
}
