package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.StringContext;

public class ResolveSymbol implements magma.app.compile.pass.Stateful {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        if (!node.is("symbol")) return new None<>();

        final var value = node.findString("value").orElse("");
        final var option = state.computeOffset(value);
        if(option.isEmpty()) return new Some<>(new Err<>(new CompileError("Unknown symbol", new StringContext(value))));

        final var offset = option.orElse(0);

        return new Some<>(new Ok<>(new Tuple<>(state, new MapNode("address").withInt("offset", offset))));
    }
}
