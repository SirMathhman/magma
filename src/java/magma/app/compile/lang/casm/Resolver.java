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

public class Resolver implements magma.app.compile.pass.Stateful {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        if (!node.is("symbol")) return new None<>();

        final var value = node.findString("value").orElse("");
        final var offset = state.computeOffset(value).orElse(0);

        return new Some<>(new Ok<>(new Tuple<>(state, new MapNode("address").withInt("offset", offset))));
    }
}
