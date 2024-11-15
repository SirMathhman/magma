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

public class TypePasser implements Passer {
    private final String type;
    private final Modifier modifier;

    public TypePasser(String type, Modifier modifier) {
        this.type = type;
        this.modifier = modifier;
    }

    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        return node.is(type)
                ? new Some<>(modifier.modify(state, node))
                : new None<>();
    }
}
