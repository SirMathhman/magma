package magma.app.compile;

import magma.api.result.Result;
import magma.app.compile.error.CompileError;

public interface Passer {
    Result<Node, CompileError> pass(Node type);
}
