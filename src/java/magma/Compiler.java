package magma;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.CompileError;
import magma.app.compile.MagmaLang;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.NodeContext;
import magma.java.JavaList;

import static magma.app.assemble.Operator.JumpByValue;
import static magma.app.compile.CASMLang.*;

public class Compiler {
    public static final String ROOT_CHILDREN = "children";
    public static final String STACK_POINTER = "__stack-pointer__";
    public static final String SPILL = "__spill__";

    static Result<Node, CompileError> compile(String source) {
        return MagmaLang.createMagmaRootRule()
                .parse(source)
                .flatMapValue(Compiler::compileRoot)
                .mapValue(Compiler::mergeIntoRoot);
    }

    private static Node mergeIntoRoot(Node program) {
        final var children = program
                .findNodeList("children")
                .orElse(new JavaList<>());

        final var count = children.stream()
                .map(Compiler::getInteger)
                .foldLeft(0, Integer::sum);

        final var instructions = new JavaList<Node>()
                .add(instruct(JumpByValue, "__start__"))
                .add(data(STACK_POINTER, count + 6))
                .add(data(SPILL, 0))
                .addAll(children);

        return new MapNode(Main.ROOT_TYPE).withNodeList0(ROOT_CHILDREN, instructions);
    }

    private static Integer getInteger(Node child) {
        if (!child.is(LABEL_TYPE)) return 1;

        return child.findNodeList(LABEL_CHILDREN)
                .map(JavaList::size)
                .orElse(0);
    }

    private static Result<Node, CompileError> compileRoot(Node root) {
        return new Err<>(new CompileError("Unknown root", new NodeContext(root)));
    }
}