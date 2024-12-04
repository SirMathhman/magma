package magma.rule;

import magma.Node;
import magma.error.CompileError;
import magma.result.Ok;
import magma.result.Result;

public class StringRule implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return new Ok<>(new Node(input));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return new Ok<>(node.value());
    }
}