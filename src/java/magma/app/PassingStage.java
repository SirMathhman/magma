package magma.app;

import magma.api.result.Result;
import magma.app.error.CompileError;

public interface PassingStage {
    Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit);
}
