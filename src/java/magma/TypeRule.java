package magma;

import magma.error.CompileError;
import magma.result.Err;
import magma.result.Result;

public record TypeRule(String type, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input).mapValue(node -> node.retype(type));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        if (node.is(type)) return childRule.generate(node);
        return new Err<>(new CompileError("Node not of type '" + type + "'", new NodeContext(node)));
    }
}
