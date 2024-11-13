package magma.app.compile.format;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.Passer;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;

import static magma.app.compile.lang.CASMLang.GROUP_AFTER_NAME;
import static magma.app.compile.lang.CASMLang.SECTION_TYPE;

public class SectionFormatter implements Passer {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (!node.is(SECTION_TYPE)) return new None<>();

        return new Some<>(new Ok<>(new Tuple<>(state, node.withString(GROUP_AFTER_NAME, " "))));
    }
}