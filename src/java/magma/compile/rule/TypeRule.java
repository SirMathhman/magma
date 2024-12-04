package magma.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.NodeContext;
import magma.compile.error.StringContext;
import magma.java.JavaList;

public record TypeRule(String type, Rule rule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return rule.parse(input)
                .mapValue(node -> node.retype(type))
                .mapErr(err -> new CompileError("Cannot assign type '" + type + "'", new StringContext(input), err));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        if (node.is(type)) return rule.generate(node);

        final var format = "Not of type '%s'";
        final var message = format.formatted(type);
        final var context = new NodeContext(node);
        return new Err<>(new CompileError(message, context));
    }
}
