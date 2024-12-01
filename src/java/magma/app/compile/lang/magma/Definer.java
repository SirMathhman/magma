package magma.app.compile.lang.magma;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.pass.Passer;

import static magma.app.compile.lang.magma.MagmaLang.*;

public class Definer implements Passer {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (!node.is(DEFINITION_TYPE)) return new None<>();

        final var name = node.findString(DEFINITION_NAME).orElse("");
        final var type = node.findNode(DEFINITION_TYPE_PROPERTY).orElse(new MapNode());

        final var defined = state.define(name, type);
        return new Some<>(new Ok<>(new Tuple<>(defined, node)));
    }
}
