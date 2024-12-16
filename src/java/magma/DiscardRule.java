package magma;

import magma.error.CompileError;
import magma.result.Ok;
import magma.result.Result;

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
