package magma.rule;

import magma.Node;
import magma.Rule;
import magma.error.CompileError;
import magma.error.StringContext;
import magma.result.Err;
import magma.result.Result;

public record FirstRule(String slice, Rule leftRule, Rule rightRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String value) {
        final var index = value.indexOf(slice());
        if (index == -1) {
            final var context = new StringContext(value);
            final var message = "Slice '%s' not present".formatted(slice());
            return new Err<>(new CompileError(message, context));
        }

        final var left = value.substring(0, index);
        final var right = value.substring(index + slice().length());

        return leftRule().parse(left)
                .and(() -> rightRule().parse(right))
                .mapValue(nodes -> nodes.left().merge(nodes.right()));
    }
}