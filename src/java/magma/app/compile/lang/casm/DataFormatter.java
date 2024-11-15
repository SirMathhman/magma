package magma.app.compile.lang.casm;

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

import static magma.app.compile.lang.CASMLang.*;

public class DataFormatter implements Passer {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (!node.is(DATA_TYPE)) return new None<>();

        final var newNode = node
                .withString(DATA_AFTER_NAME, " ")
                .withString(DATA_BEFORE_VALUE, " ");

        return new Some<>(new Ok<>(new Tuple<>(state, newNode)));
    }
}
