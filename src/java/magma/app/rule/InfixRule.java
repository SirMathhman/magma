package magma.app.rule;

import magma.app.compile.Node;
import magma.app.error.FormattedError;
import magma.app.error.StringContext;
import magma.app.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Result;

public record InfixRule(Rule leftRule, String slice, Rule rightRule) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(String input) {
        final var index = input.indexOf(slice);
        if (index == -1)
            return new Err<>(new CompileError("Infix '" + slice + "' not present", new StringContext(input)));

        final var left = input.substring(0, index);
        final var right = input.substring(index + slice.length());

        return leftRule.parse(left).flatMapValue(leftNode -> rightRule.parse(right).mapValue(leftNode::merge));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return leftRule.generate(node).flatMapValue(leftValue -> rightRule.generate(node).mapValue(rightValue -> leftValue + slice + rightValue));
    }
}
