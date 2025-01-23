package magma.app;

import magma.api.result.Result;
import magma.app.error.CompileError;

public interface Passer {
    Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit);

    Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit);
}
