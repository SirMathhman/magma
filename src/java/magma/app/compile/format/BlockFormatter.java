package magma.app.compile.format;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.Passer;
import magma.app.compile.error.CompileError;

import static magma.app.compile.lang.CASMLang.*;

public class BlockFormatter implements Passer {
    @Override
    public Option<Result<Node, CompileError>> afterPass(Node node) {
        if (node.is(BLOCK_TYPE)) {
            return node.mapNodeList(CHILDREN, list -> {
                return new Ok<>(list.stream()
                        .map(child -> child.withString(GROUP_BEFORE_CHILD, "\n\t"))
                        .toList());
            });
        }

        return new None<>();
    }
}
