package magma.api.compile.rule;

import magma.api.compile.Node;
import magma.api.Tuple;
import magma.api.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

public record SplitRule(Rule leftRule, String infix, Rule rightRule) implements Rule {
    static Result<Tuple<String, String>, CompileError> split(String input, String infix) {
        final var index = input.indexOf(infix);
        if (index == -1) return new Err<>(new CompileError("Infix '" + infix + "' not present", input));

        final var left = input.substring(0, index);
        final var right = input.substring(index + infix.length());
        return new Ok<>(new Tuple<>(left, right));
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return split(input, infix()).flatMapValue(tuple -> {
            return leftRule().parse(tuple.left()).and(() -> {
                return rightRule().parse(tuple.right());
            }).mapValue(Tuple.merge(Node::merge));
        });
    }
}