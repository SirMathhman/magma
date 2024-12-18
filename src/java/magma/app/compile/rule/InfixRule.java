package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
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

    @Override
    public Result<Node, FormattedError> parse(Input input) {
        Input input1 = new Input(input.input());
        return splitter.split(input1).map(tuple1 -> tuple1
                .mapLeft(Input::input)
                .mapRight(Input::input)).map(tuple -> {
            var left = tuple.left();
            var right = tuple.right();
            return leftRule.parse(new Input(left)).flatMapValue(leftNode -> rightRule.parse(new Input(right)).mapValue(leftNode::merge));
        }).orElseGet(() -> new Err<>(splitter.createError(new Input(input.input()))));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return leftRule.generate(node).flatMapValue(leftValue -> rightRule.generate(node).mapValue(rightValue -> splitter.merge(leftValue, rightValue)));
    }
}
