package magma.app.compile.rule;

import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.app.error.FormattedError;
import magma.app.error.NodeContext;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.error.StringContext;

public record TypeRule(String type, Rule childRule) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(String input) {
        return childRule.parse(input)
                .mapValue(node -> node.retype(type))
                .mapErr(err -> new CompileError("Failed to assign type '" + type + "'", new StringContext(input), err));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        if (node.is(type)) return childRule.generate(node);
        return new Err<>(new CompileError("Node not of type '" + type + "'", new NodeContext(node)));
    }
}
