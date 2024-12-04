package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

public record ExactRule(String slice) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (input.equals(slice)) return new Ok<>(new Node());
        final var format = "Input does not match exactly '%s'";
        final var message = format.formatted(slice);
        final var context = new StringContext(input);
        return new Err<>(new CompileError(message, context));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return new Ok<>(slice);
    }
}