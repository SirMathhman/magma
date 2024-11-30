package magma.app.compile.rule;

import magma.app.compile.Node;
import magma.app.compile.error.StringContext;
import magma.app.compile.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Result;

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
