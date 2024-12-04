package magma.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;

public final class InfixRule implements Rule {
    private final String infix;
    private final Rule leftRule;
    private final Rule rightRule;

    public InfixRule(Rule leftRule, String infix, Rule rightRule) {
        this.infix = infix;
        this.leftRule = leftRule;
        this.rightRule = rightRule;
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        final var index = input.indexOf(infix);
        if (index == -1) {
            final var format = "Slice not present '%s'";
            final var message = format.formatted(infix);
            final var context = new StringContext(input);
            return new Err<>(new CompileError(message, context));
        } else {
            final var left = input.substring(0, index);
            final var right = input.substring(index + infix.length());

            return leftRule.parse(left)
                    .and(() -> rightRule.parse(right))
                    .mapValue(tuple -> tuple.left().merge(tuple.right()));
        }
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return leftRule.generate(node)
                .and(() -> rightRule.generate(node))
                .mapValue(tuple -> tuple.left() + infix + tuple.right());
    }
}