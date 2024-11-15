package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.pass.Folder;

import static magma.app.compile.lang.CASMLang.BLOCK_BEFORE_CHILD;

public class InstructionFormatter implements Folder {
    @Override
    public Result<Tuple<State, Node>, CompileError> afterModify(State state, Node node) {
        return new Ok<>(new Tuple<>(state, node.withString(BLOCK_BEFORE_CHILD, "\n" + "\t".repeat(state.depth()))));
    }
}
