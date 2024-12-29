package magma.compile.rule.split;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.rule.Rule;

import java.util.Collections;

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
        return splitter.split(input)
                .map(tuple -> parseAndMerge(tuple.left(), tuple.right()))
                .orElseGet(() -> new Err<>(splitter.createError(input, Collections.emptyList())));
    }

    private Result<Node, CompileError> parseAndMerge(String left, String right) {
        return leftRule.parse(left)
                .and(() -> rightRule.parse(right))
                .mapValue(tuple -> tuple.left().merge(tuple.right()));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return leftRule.generate(node).flatMapValue(
                generatedLeft -> rightRule.generate(node).mapValue(
                        generatedRight -> splitter.merge(generatedLeft, generatedRight)));
    }
}
