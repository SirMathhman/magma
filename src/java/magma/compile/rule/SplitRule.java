package magma.compile.rule;

import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;

import java.util.ArrayList;

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
        final var pairs = splitter.split(input).toList();

        // TODO: duplicate logic in OrRule
        var errors = new ArrayList<CompileError>();
        for (Tuple<String, String> pair : pairs) {
            final var parsedLeft = leftRule.parse(pair.left());
            final var parsedRight = rightRule.parse(pair.right());
            final var result = parsedLeft.and(() -> parsedRight).mapValue(tuple -> tuple.left().merge(tuple.right()));
            if (result.isOk()) {
                return result;
            } else {
                errors.add(result.findError().orElseThrow());
            }
        }

        return new Err<>(splitter.createError(input, errors));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return leftRule.generate(node).flatMapValue(
                generatedLeft -> rightRule.generate(node).mapValue(
                        generatedRight -> splitter.merge(generatedLeft, generatedRight)));
    }
}
