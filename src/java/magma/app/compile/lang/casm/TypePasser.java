package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.Passer;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.pass.Folder;

public class TypePasser implements Passer {
    private final String type;
    private final Folder folder;

    public TypePasser(String type, Folder folder) {
        this.type = type;
        this.folder = folder;
    }

    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        return node.is(type)
                ? new Some<>(folder.modify(state, node))
                : new None<>();
    }
}
