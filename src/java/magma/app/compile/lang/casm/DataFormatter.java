package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CASMLang;

public class DataFormatter implements Modifier {
    @Override
    public Result<Tuple<State, Node>, CompileError> modify(State state, Node node) {
        final var newNode = node
                .withString(CASMLang.DATA_AFTER_NAME, " ")
                .withString(CASMLang.DATA_BEFORE_VALUE, " ");

        return new Ok<>(new Tuple<>(state, newNode));
    }
}