package magma.app.compile;

import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.java.JavaStreams;

import java.util.List;

public record CompoundPassingStage(List<PassingStage> stages) implements PassingStage {
    @Override
    public Result<Node, CompileError> pass(Node node) {
        return JavaStreams.fromList(stages).foldLeftToResult(node, (node1, passingStage) -> passingStage.pass(node1));
    }
}
