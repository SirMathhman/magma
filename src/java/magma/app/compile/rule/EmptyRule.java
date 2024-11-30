package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.StringContext;

public class EmptyRule implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (input.isEmpty()) return new Ok<>(new MapNode());
        return new Err<>(new CompileError("Not empty", new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return new Ok<>("");
    }
}