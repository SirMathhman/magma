package magma.app.compile.pass;

import magma.Main;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Compiler;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.lang.CASMLang;
import magma.java.JavaList;

import static magma.app.assemble.Operator.JumpByValue;
import static magma.app.compile.lang.CASMLang.*;

public class Starter implements PassingStage {
    public static final String START_LABEL = "__start__";

    private static int countSize(Node child) {
        if (!child.is(LABEL_TYPE)) return 1;

        return child.findNodeList(LABEL_CHILDREN)
                .map(JavaList::size)
                .orElse(0);
    }

    @Override
    public Result<Node, CompileError> pass(Node root) {
        if (!root.is(CASMLang.PROGRAM_TYPE)) {
            final var context = new NodeContext(root);
            final var error = new CompileError("Not a program", context);
            return new Err<>(error);
        }

        final var childrenOption = root.findNodeList(PROGRAM_CHILDREN);
        if (childrenOption.isEmpty()) {
            final var context = new NodeContext(root);
            final var error = new CompileError("No children present", context);
            return new Err<>(error);
        }
        final var children = childrenOption.orElse(new JavaList<>());

        final var totalSize = children.stream()
                .map(Starter::countSize)
                .foldLeft(0, Integer::sum);

        final var instructions = new JavaList<Node>()
                .add(instruct(JumpByValue, START_LABEL))
                .add(data(Compiler.STACK_POINTER, totalSize + 6))
                .add(data(Compiler.SPILL, 0))
                .addAll(children);

        return new Ok<>(new MapNode(Main.ROOT_TYPE).withNodeList(Compiler.ROOT_CHILDREN, instructions));
    }
}
