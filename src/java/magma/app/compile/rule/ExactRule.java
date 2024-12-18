package magma.app.compile.rule;

import magma.app.Input;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.FormattedError;
import magma.app.error.InputContext;
import magma.app.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

public record ExactRule(String value) implements Rule {
    private Result<Node, FormattedError> parse0(String input) {
        return input.equals(value)
                ? new Ok<>(new MapNode())
                : new Err<>(new CompileError("Input did not match exact '" + value + "'", new InputContext(new Input(input, 0, input.length()))));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return new Ok<>(value());
    }

    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return parse0(input.slice());
    }
}