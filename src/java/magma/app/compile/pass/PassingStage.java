package magma.app.compile.pass;

import magma.api.result.Result;
import magma.app.compile.node.Node;
import magma.app.error.CompileError;

public interface PassingStage {
    Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit);
}
