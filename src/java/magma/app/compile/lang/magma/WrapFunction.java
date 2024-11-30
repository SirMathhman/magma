package magma.app.compile.lang.magma;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.pass.PassingStage;
import magma.java.JavaList;

import static magma.app.compile.lang.magma.MagmaLang.*;
import static magma.app.compile.pass.Starter.START_LABEL;

public class WrapFunction implements PassingStage {
    @Override
    public Result<Node, CompileError> pass(Node root) {
        if (!root.is(ROOT_TYPE)) return new Err<>(new CompileError("Not a root", new NodeContext(root)));

        final var children = root.findNodeList(ROOT_CHILDREN).orElse(new JavaList<>());
        final var block = new MapNode(BLOCK_TYPE).withNodeList(MagmaLang.BLOCK_CHILDREN, children);

        return new Ok<>(new MapNode("function")
                .withString("name", START_LABEL)
                .withNode("value", block));
    }
}
