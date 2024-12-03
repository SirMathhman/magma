package magma.app.compile.lang.magma;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;

public interface Stateless {
    default Result<Node, CompileError> beforePass(Node node) {
        return new Ok<>(node);
    }

    default Node afterPass(Node node) {
        return node;
    }
}
