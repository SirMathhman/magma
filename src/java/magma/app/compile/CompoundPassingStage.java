package magma.app.compile;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.java.JavaList;

public final class CompoundPassingStage implements PassingStage {
    private final JavaList<PassingStage> stages;

    public CompoundPassingStage(JavaList<PassingStage> stages) {
        this.stages = stages;
    }

    @Override
    public Result<Tuple<State, Node>, CompileError> pass(State state, Node node) {
        final var initial = new Tuple<>(state, node);
        return stages.stream().foldLeftToResult(initial, (current, stage) -> {
            final var currentState = current.left();
            final var currentValue = current.right();
            return stage.pass(currentState, currentValue);
        });
    }
}
