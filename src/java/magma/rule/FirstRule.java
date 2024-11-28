package magma.rule;

import magma.Node;
import magma.error.StringContext;
import magma.error.CompileError;
import magma.result.Err;
import magma.result.Result;

public record FirstRule(Rule leftRule, String slice, Rule rightRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        final var index = input.indexOf(slice);
        if(index == -1) return new Err<>( new CompileError("Slice '" + slice + "' not present", new StringContext(input)));

        final var left = input.substring(0, index);
        final var right = input.substring(index + slice.length());

        return leftRule.parse(left).and(() -> rightRule.parse(right)).mapValue(tuple -> tuple.left().merge(tuple.right()));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return leftRule.generate(node).and(() -> rightRule.generate(node)).mapValue(tuple -> tuple.left() + slice + tuple.right());
    }
}
