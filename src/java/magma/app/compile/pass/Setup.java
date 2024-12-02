package magma.app.compile.pass;

import magma.Main;
import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Compiler;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.lang.casm.CASMLang;
import magma.java.JavaList;

import static magma.app.compile.lang.casm.CASMLang.*;
import static magma.app.compile.lang.casm.assemble.Operator.JumpByValue;

public class Setup implements PassingStage {
    public static final String START_LABEL = "__start__";

    private static int countSize(Node child) {
        if (!child.is(LABEL_TYPE)) return 1;

        return child.findNodeList(LABEL_CHILDREN)
                .map(JavaList::size)
                .orElse(0);
    }

    @Override
    public Result<Tuple<State, Node>, CompileError> pass(State state, Node root) {
        final JavaList<Node> children;
        if (root.is(LABEL_TYPE)) {
            children = new JavaList<Node>().add(root);
        } else {
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
            children = childrenOption.orElse(new JavaList<>());
        }

        final var totalSize = children.stream()
                .map(Setup::countSize)
                .foldLeft(0, Integer::sum);

        final var instructions = new JavaList<Node>()
                .add(instruct(JumpByValue, START_LABEL))
                .add(data(Compiler.STACK_POINTER, totalSize + 6))
                .add(data(Compiler.SPILL, 0))
                .addAll(children);

        final var node = new MapNode(Main.ROOT_TYPE).withNodeList(Compiler.ROOT_CHILDREN, instructions);
        return new Ok<>(new Tuple<>(state, node));
    }
}
