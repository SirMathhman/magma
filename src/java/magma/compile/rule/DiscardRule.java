package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.api.result.Ok;
import magma.api.result.Result;

public class DiscardRule implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return new Ok<>(new Node());
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return new Ok<>("");
    }
}
