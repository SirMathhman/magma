package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.pass.Folder;

public class StatelessFolder implements Folder {
    private final Modifier modifier;

    public StatelessFolder(Modifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public Result<Tuple<State, Node>, CompileError> afterModify(State state, Node node) {
        return new Ok<>(new Tuple<>(state, modifier.modify(node)));
    }
}