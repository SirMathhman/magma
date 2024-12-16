package magma;

import magma.error.CompileError;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

public record ExactRule(String value) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return input.equals(value)
                ? new Ok<>(new MapNode())
                : new Err<>(new CompileError("Input did not match exact '" + value + "'", new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return new Ok<>(value());
    }
}