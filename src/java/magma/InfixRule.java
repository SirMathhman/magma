package magma;

import magma.error.CompileError;
import magma.result.Err;
import magma.result.Result;

public record InfixRule(Rule leftRule, String slice, Rule rightRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        final var index = input.indexOf(slice);
        if (index == -1)
            return new Err<>(new CompileError("Infix '" + slice + "' not present", new StringContext(input)));

        final var left = input.substring(0, index);
        final var right = input.substring(index + slice.length());

        return leftRule.parse(left).flatMapValue(leftNode -> rightRule.parse(right).mapValue(leftNode::merge));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return leftRule.generate(node).flatMapValue(leftValue -> rightRule.generate(node).mapValue(rightValue -> leftValue + slice + rightValue));
    }
}
