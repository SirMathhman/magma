package magma.compile.rule.string;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.NodeContext;
import magma.compile.rule.Rule;

public record StringRule(String propertyKey) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return new Ok<>(new Node().withString(propertyKey, input));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var optional = node.findString(propertyKey);
        if (optional.isEmpty())
            return new Err<>(new CompileError("String '" + propertyKey + "' not present", new NodeContext(node)));

        return new Ok<>(optional.get());
    }
}
