package magma.app.compile.pass;

import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.Node;

public interface PassingStage {
    Result<Node, CompileError> pass(Node root);
}
