package magma.app.compile.lang.casm;

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
import magma.app.compile.lang.common.CommonLang;
import magma.java.JavaList;

public class FlattenGoto implements magma.app.compile.pass.Passer {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        if (!node.is("goto")) return new None<>();

        final var location = node.findString("location").orElse("");
        final var distance = state.computeDistance(location).orElse(0);

        return new Some<>(new Ok<>(new Tuple<>(state, CommonLang.toGroup(new JavaList<Node>()
                .add(new MapNode("move-stack-pointer").withInt("distance", distance))
                .addAll(node.findNodeList("children").orElse(new JavaList<>()))
                .add(new MapNode("move-stack-pointer").withInt("distance", -distance))))));
    }
}