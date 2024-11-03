package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;

public record TypeRule(String type, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input).flatMapValue(this::retype);
    }

    private Result<Node, CompileError> retype(Node node) {
        return node.retype(type)
                .<Result<Node, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("Node already has a type", new NodeContext(node))));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        if (!node.is(type))
            return new Err<>(new CompileError("Type '" + type + "' not present", new NodeContext(node)));

        return childRule.generate(node);
    }
}
