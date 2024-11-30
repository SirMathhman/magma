package magma.app.compile.pass;

import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.Node;
import magma.java.JavaList;

public record CompoundPassingStage(JavaList<PassingStage> stages) implements PassingStage {
    @Override
    public Result<Node, CompileError> pass(Node root) {
        return stages.stream().foldLeftToResult(root, (node, stage) -> stage.pass(node));
    }
}
