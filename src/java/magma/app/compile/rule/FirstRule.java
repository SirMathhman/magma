package magma.app.compile.rule;

import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.StringContext;

public record FirstRule(Rule leftRule, String slice, Rule rightRule) implements Rule {
    private static Result<Node, CompileError> merge(String input, Tuple<Node, Node> tuple) {
        return tuple.left().merge(tuple.right())
                .<Result<Node, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("Types present for both nodes.", new StringContext(input))));
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        final var index = input.indexOf(slice);
        if (index == -1)
            return new Err<>(new CompileError("Slice '" + slice + "' not present.", new StringContext(input)));

        final var left = input.substring(0, index);
        final var right = input.substring(index + slice.length());

        return leftRule.parse(left).and(() -> rightRule.parse(right)).flatMapValue(tuple -> merge(input, tuple));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return leftRule.generate(node)
                .and(() -> rightRule.generate(node))
                .mapValue(tuple -> tuple.left() + slice + tuple.right());
    }
}
