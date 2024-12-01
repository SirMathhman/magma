package magma.app.compile.pass;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.Node;
import magma.java.JavaList;

public record CompoundPassingStage(JavaList<PassingStage> stages) implements PassingStage {
    @Override
    public Result<Tuple<State, Node>, CompileError> pass(State state, Node root) {
        return stages.stream().foldLeftToResult(new Tuple<>(state, root), (node, stage) -> stage.pass(node.left(), node.right()));
    }
}
