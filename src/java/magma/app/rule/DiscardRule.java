package magma.app.rule;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.api.result.Ok;
import magma.api.result.Result;

public class DiscardRule implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return new Ok<>(new MapNode());
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return new Ok<>("");
    }
}
