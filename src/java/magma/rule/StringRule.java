package magma.rule;

import magma.Node;
import magma.error.NodeContext;
import magma.error.CompileError;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

public record StringRule(String propertyKey) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return new Ok<>(new Node().withString(propertyKey, input));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var option = node.findString(propertyKey);
        if(option.isEmpty()) return new Err<>(new CompileError("String '" + propertyKey + "' not present", new NodeContext(node)));

        return new Ok<>(option.orElse(""));
    }
}
