package magma.app.compile.rule;

import magma.api.result.Err;
import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

import java.util.ArrayList;

public final class LocatingRule implements Rule {
    private final Rule leftRule;
    private final Rule rightRule;
    private final Locator locator;

    public LocatingRule(Rule leftRule, Locator locator, Rule rightRule) {
        this.leftRule = leftRule;
        this.rightRule = rightRule;
        this.locator = locator;
    }

    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        final var occurrences = locator.locate(input).toList();

        var errors = new ArrayList<RuleResult<Node, ParseException>>();
        for (Integer occurrence : occurrences) {
            var result = getNodeParseExceptionRuleResult(input, occurrence);
            if (result.isValid()) {
                return result;
            } else {
                errors.add(result);
            }
        }

        if (occurrences.isEmpty()) {
            return new RuleResult<>(new Err<>(new ParseException("No occurrence found of '" + locator.slice() + "'", input)));
        } else if (occurrences.size() == 1) {
            return errors.getFirst();
        } else {
            return new RuleResult<>(new Err<>(new ParseException("Failed to find a valid combination of slice '" + locator.slice() + "'", input)), errors);
        }
    }

    private RuleResult<Node, ParseException> getNodeParseExceptionRuleResult(String input, int index) {
        final var left = input.substring(0, index);
        final var right = input.substring(index + locator.slice().length());

        final var leftResult = leftRule.parse(left);
        if (leftResult.isError()) return leftResult;

        final var rightResult = rightRule.parse(right);
        if (rightResult.isError()) return rightResult;

        return new RuleResult<>(leftResult.result().and(rightResult::result).mapValue(tuple -> tuple.left().merge(tuple.right())));
    }


    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        final var leftResult = leftRule.generate(node);
        if (leftResult.isError()) return leftResult.wrapErr(new GenerateException("Invalid left", node));

        final var rightResult = rightRule.generate(node);
        if (rightResult.isError()) return rightResult.wrapErr(new GenerateException("Invalid right", node));

        return new RuleResult<>(leftResult.result().and(rightResult::result).mapValue(tuple -> tuple.left() + locator.slice() + tuple.right()));
    }
}
