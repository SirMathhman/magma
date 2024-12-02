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
import magma.app.compile.pass.Stateful;

public class Definer implements Stateful {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (!node.is("define")) return new None<>();

        final var name = node.findString("name").orElse("");
        final var type = node.findNode("type").orElse(new MapNode());

        final var defined = state.define(name, type);
        return new Some<>(new Ok<>(new Tuple<>(defined, CommonLang.createEmptyGroup())));
    }
}
