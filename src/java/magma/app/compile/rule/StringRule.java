package magma.app.compile.rule;

import magma.app.Input;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.app.error.FormattedError;
import magma.app.error.NodeContext;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

public record StringRule(String propertyKey) implements Rule {
    private Result<Node, FormattedError> parse0(String input) {
        return new Ok<>(new MapNode().withString(propertyKey, input));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return node.findString(propertyKey)
                .<Result<String, FormattedError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + propertyKey + "' not present", new NodeContext(node))));
    }

    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return parse0(input.slice());
    }
}
