package magma.app.compile.pass;

import magma.app.compile.Compiler;
import magma.Main;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.java.JavaList;

import static magma.app.assemble.Operator.JumpByValue;
import static magma.app.compile.lang.CASMLang.*;

public class Initializer implements Passer {
    private static int countSize(Node child) {
        if (!child.is(LABEL_TYPE)) return 1;

        return child.findNodeList(LABEL_CHILDREN)
                .map(JavaList::size)
                .orElse(0);
    }

    @Override
    public Option<Result<Node, CompileError>> afterNode(Node node) {
        final var children = node
                .findNodeList("children")
                .orElse(new JavaList<>());

        final var totalSize = children.stream()
                .map(Initializer::countSize)
                .foldLeft(0, Integer::sum);

        final var instructions = new JavaList<Node>()
                .add(instruct(JumpByValue, "__start__"))
                .add(data(Compiler.STACK_POINTER, totalSize + 6))
                .add(data(Compiler.SPILL, 0))
                .addAll(children);

        return new Some<>(new Ok<>(new MapNode(Main.ROOT_TYPE).withNodeList(Compiler.ROOT_CHILDREN, instructions)));
    }
}
