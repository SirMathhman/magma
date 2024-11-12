package magma.app.compile.format;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.Passer;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;

import java.util.List;

import static magma.app.compile.lang.CASMLang.*;

public class BlockFormatter implements Passer {
    private static Result<List<Node>, CompileError> attachPadding(List<Node> list) {
        return new Ok<>(list.stream()
                .map(child -> child.withString(BLOCK_BEFORE_CHILD, "\n\t"))
                .toList());
    }

    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (!node.is(BLOCK_TYPE)) return new None<>();

        return node.withString(BLOCK_AFTER_CHILDREN, "\n")
                .mapNodeList(CHILDREN, BlockFormatter::attachPadding)
                .map(result -> result.mapValue(value -> new Tuple<>(state, value)));
    }
}
