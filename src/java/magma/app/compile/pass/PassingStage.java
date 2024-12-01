package magma.app.compile.pass;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.Node;

public interface PassingStage {
    Result<Tuple<State, Node>, CompileError> pass(State state, Node root);
}
