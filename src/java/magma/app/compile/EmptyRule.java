package magma.app.compile;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.rule.Rule;
import magma.app.error.StringContext;

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
