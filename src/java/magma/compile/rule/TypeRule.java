package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.NodeContext;
import magma.api.result.Err;
import magma.api.result.Result;

public record TypeRule(String type, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input).mapValue(node -> node.retype(type));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        if (!node.is(type))
            return new Err<>(new CompileError("Type '" + type + "' not present", new NodeContext(node)));
        return childRule.generate(node);
    }
}
