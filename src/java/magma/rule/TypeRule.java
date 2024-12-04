package magma.rule;

import magma.Node;
import magma.error.CompileError;
import magma.result.Result;

public record TypeRule(String type, Rule rule)  implements Rule{
    @Override
    public Result<Node, CompileError> parse(String input) {
        return rule.parse(input).mapValue(node -> node.retype(type));
    }
}
