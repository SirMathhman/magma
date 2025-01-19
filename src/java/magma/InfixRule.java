package magma;

import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.rule.Rule;
import magma.app.rule.StringRule;

public record InfixRule(StringRule leftRule, String infix, StringRule rightRule) implements Rule {
    @Override
    public Result<String, CompileError> generate(Node node) {
        return leftRule().apply(node).and(() -> rightRule().apply(node)).mapValue(tuple -> {
            return tuple.left() + infix() + tuple.right();
        });
    }
}