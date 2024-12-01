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

import static magma.app.compile.lang.magma.MagmaLang.*;

public class FlattenAssignment implements magma.app.compile.pass.Passer {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (!node.is(ASSIGNMENT_TYPE)) return new None<>();

        final var variable = node.findNode(ASSIGNMENT_VARIABLE).orElse(new MapNode());
        final var length = variable.findNode("type")
                .flatMap(type -> type.findInt("length"))
                .orElse(0);

        final var expression = node.findNode(ASSIGNMENT_EXPRESSION).orElse(new MapNode());

        final var result = new MapNode("move")
                .withInt("length", length)
                .withNode("destination", variable)
                .withNode("source", expression);

        return new Some<>(new Ok<>(new Tuple<>(state, result)));
    }
}
