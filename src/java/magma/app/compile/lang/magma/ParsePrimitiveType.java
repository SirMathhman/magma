package magma.app.compile.lang.magma;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;

public class ParsePrimitiveType implements Stateless {
    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        return new Ok<>(node.withInt("length", 1));
    }
}
