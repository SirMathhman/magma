package magma.app.compile.pass;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;

public interface Passer {
    default Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        return new None<>();
    }

    default Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        return new None<>();
    }
}
