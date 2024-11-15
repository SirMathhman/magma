package magma.app.compile.pass;

import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;

public interface Folder {
    default Result<Tuple<State, Node>, CompileError> afterModify(State state, Node node) {
        return new Ok<>(new Tuple<>(state, node));
    }

    default Result<Tuple<State, Node>, CompileError> beforeModify(State state, Node node) {
        return new Ok<>(new Tuple<>(state, node));
    }
}
