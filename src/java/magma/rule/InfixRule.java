package magma.rule;

import magma.Node;
import magma.error.CompileError;
import magma.error.StringContext;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

public record InfixRule(String infix) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (input.contains(infix())) {
            return new Ok<>(new Node());
        }
        return new Err<>(new CompileError("Slice not present '" + infix() + "'", new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return new Ok<>(infix);
    }
}