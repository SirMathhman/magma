package magma.app.pass;

import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;

public interface PassingStage {
    Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit);
}
