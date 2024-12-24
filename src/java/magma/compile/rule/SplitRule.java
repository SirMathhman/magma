package magma.compile.rule;

import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;

public final class SplitRule implements Rule {
    private final Rule leftRule;
    private final Rule rightRule;
    private final Splitter splitter;

    public SplitRule(Rule leftRule, Splitter splitter, Rule rightRule) {
        this.leftRule = leftRule;
        this.rightRule = rightRule;
        this.splitter = splitter;
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return splitter.split(input).flatMapValue(
                tuple -> leftRule.parse(tuple.left()).flatMapValue(
                        parsedLeft -> rightRule.parse(tuple.right()).mapValue(parsedLeft::merge)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return leftRule.generate(node).flatMapValue(
                generatedLeft -> rightRule.generate(node).mapValue(
                        generatedRight -> splitter.merge(generatedLeft, generatedRight)));
    }
}
