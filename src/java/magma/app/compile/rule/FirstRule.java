package magma.app.compile.rule;

import magma.api.result.Err;
import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

public record FirstRule(Rule leftRule, String slice, Rule rightRule) implements Rule {
    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        final var index = input.indexOf(slice);
        if (index == -1)
            return new RuleResult<>(new Err<>(new ParseException("Slice '" + slice + "' not present", input)));

        final var left = input.substring(0, index);
        final var right = input.substring(index + slice.length());

        final var leftResult = leftRule.parse(left);
        if (leftResult.isError()) return leftResult.wrapErr(new ParseException("Invalid left", left));

        final var rightResult = rightRule.parse(right);
        if (rightResult.isError()) return rightResult.wrapErr(new ParseException("Invalid right", right));

        return new RuleResult<>(leftResult.result().and(rightResult::result).mapValue(tuple -> tuple.left().merge(tuple.right())));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        final var leftResult = leftRule.generate(node);
        if (leftResult.isError()) return leftResult.wrapErr(new GenerateException("Invalid left", node));

        final var rightResult = rightRule.generate(node);
        if (rightResult.isError()) return rightResult.wrapErr(new GenerateException("Invalid right", node));

        return new RuleResult<>(leftResult.result().and(rightResult::result).mapValue(tuple -> tuple.left() + slice + tuple.right()));
    }
}
