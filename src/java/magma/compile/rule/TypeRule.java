package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.api.result.Result;

public record TypeRule(String type, Rule rule)  implements Rule{
    @Override
    public Result<Node, CompileError> parse(String input) {
        return rule.parse(input).mapValue(node -> node.retype(type));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return rule.generate(node);
    }
}
