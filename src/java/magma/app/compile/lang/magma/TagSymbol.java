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

import static magma.app.compile.lang.magma.MagmaLang.SYMBOL_TYPE;
import static magma.app.compile.lang.magma.MagmaLang.SYMBOL_VALUE;

public class TagSymbol implements Passer {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        if(!node.is(SYMBOL_TYPE)) return new None<>();

        final var value = node.findString(SYMBOL_VALUE).orElse("");
        final var type = state.lookup(value).orElse(new MapNode());

        final var withType = node.withNode("type", type);
        return new Some<>(new Ok<>(new Tuple<>(state, withType)));
    }
}
