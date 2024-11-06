package magma.app.compile;

import magma.api.result.Result;
import magma.app.compile.error.CompileError;

public interface PassingStage {
    Result<Node, CompileError> pass(Node node);
}
