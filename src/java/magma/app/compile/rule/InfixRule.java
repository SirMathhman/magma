package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.Input;
import magma.app.compile.Node;
import magma.app.error.FormattedError;

public final class InfixRule implements Rule {
    private final Rule leftRule;
    private final Splitter splitter;
    private final Rule rightRule;

    public InfixRule(Rule leftRule, Splitter splitter, Rule rightRule) {
        this.leftRule = leftRule;
        this.splitter = splitter;
        this.rightRule = rightRule;
    }

    private Result<Node, FormattedError> parse0(String input) {
        return splitter.split(input).map(tuple -> {
            var left = tuple.left();
            var right = tuple.right();

            return leftRule.parse(new Input(left, 0, left.length())).flatMapValue(leftNode -> rightRule.parse(new Input(right, 0, right.length())).mapValue(leftNode::merge));
        }).orElseGet(() -> new Err<>(splitter.createError(input)));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return leftRule.generate(node).flatMapValue(leftValue -> rightRule.generate(node).mapValue(rightValue -> splitter.merge(leftValue, rightValue)));
    }

    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return parse0(input.slice());
    }
}
