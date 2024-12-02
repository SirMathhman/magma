package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.pass.Stateful;
import magma.java.JavaList;

public class PruneDefine implements Stateful {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        if (!node.is("define")) return new None<>();

        final var group = CommonLang.toGroup(new JavaList<>());
        return new Some<>(new Ok<>(new Tuple<>(state, group)));
    }
}
