package magma.app.compile.rule;

import magma.api.java.MutableJavaList;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.ContextDetail;
import magma.app.error.FormattedError;
import magma.app.error.InputContext;
import magma.app.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

public record ExactRule(String value) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return input.input().equals(value)
                ? new Ok<>(new MapNode())
                : new Err<>(new CompileError(new ContextDetail("Input did not match exact '" + value + "'", new InputContext(new Input(input.input()))), new MutableJavaList<>()));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return new Ok<>(value());
    }
}