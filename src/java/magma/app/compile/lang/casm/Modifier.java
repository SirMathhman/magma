package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;

public interface Modifier {
    Result<Tuple<State, Node>, CompileError> modify(State state, Node node);
}
