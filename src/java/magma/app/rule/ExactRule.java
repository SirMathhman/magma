package magma.app.rule;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.FormattedError;
import magma.app.error.StringContext;
import magma.app.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

public record ExactRule(String value) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(String input) {
        return input.equals(value)
                ? new Ok<>(new MapNode())
                : new Err<>(new CompileError("Input did not match exact '" + value + "'", new StringContext(input)));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return new Ok<>(value());
    }
}