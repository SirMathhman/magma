package magma.app.compile.pass;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;

public interface PassingStage {
    Result<Tuple<State, Node>, CompileError> pass(State state, Node node);
}
