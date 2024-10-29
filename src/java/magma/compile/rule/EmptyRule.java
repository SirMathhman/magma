package magma.compile.rule;

import magma.compile.CompileError;
import magma.compile.MapNode;
import magma.compile.Node;
import magma.core.String_;
import magma.core.result.Err;
import magma.core.result.Ok;
import magma.core.result.Result;
import magma.java.JavaString;

public class EmptyRule implements Rule {
    @Override
    public Result<Node, CompileError> parse(String_ input) {
        if (input.isEmpty()) return new Ok<>(new MapNode());
        final var error = CompileError.create("Input is not empty", input);
        return new Err<>(error);
    }

    @Override
    public Result<String_, CompileError> generate(Node node) {
        return new Ok<>(JavaString.EMPTY);
    }
}
