package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

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