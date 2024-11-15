package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.pass.Folder;

import static magma.app.compile.lang.CASMLang.BLOCK_AFTER_CHILDREN;

public class BlockPasser implements Folder {
    @Override
    public Result<Tuple<State, Node>, CompileError> beforeModify(State state, Node node) {
        return new Ok<>(new Tuple<>(state.enter(), node));
    }

    @Override
    public Result<Tuple<State, Node>, CompileError> afterModify(State state, Node node) {
        final var exited = state.exit();
        final var afterChildren = "\n" + "\t".repeat(exited.depth());
        final var newNode = node.withString(BLOCK_AFTER_CHILDREN, afterChildren);
        return new Ok<>(new Tuple<>(exited, newNode));
    }
}
