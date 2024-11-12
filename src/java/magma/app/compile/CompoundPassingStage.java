package magma.app.compile;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.java.JavaStreams;

import java.util.List;

public record CompoundPassingStage(List<PassingStage> stages) implements PassingStage {
    @Override
    public Result<Tuple<State, Node>, CompileError> pass(State state, Node node) {
        return JavaStreams.fromList(stages)
                .foldLeftToResult(new Tuple<>(state, node), (tuple, passingStage) -> passingStage.pass(tuple.left(), tuple.right()));
    }
}
