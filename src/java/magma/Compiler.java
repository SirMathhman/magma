package magma;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.ResultStream;
import magma.app.compile.CompileError;
import magma.app.compile.MagmaLang;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
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
                .flatMapValue(Compiler::pass)
                .mapValue(Compiler::mergeIntoRoot);
    }

    private static Node mergeIntoRoot(Node program) {
        final var children = program
                .findNodeList("children")
                .orElse(new JavaList<>());

        final var totalSize = children.stream()
                .map(Compiler::countSize)
                .foldLeft(0, Integer::sum);

        final var instructions = new JavaList<Node>()
                .add(instruct(JumpByValue, "__start__"))
                .add(data(STACK_POINTER, totalSize + 6))
                .add(data(SPILL, 0))
                .addAll(children);

        return new MapNode(Main.ROOT_TYPE).withNodeList(ROOT_CHILDREN, instructions);
    }

    private static int countSize(Node child) {
        if (!child.is(LABEL_TYPE)) return 1;

        return child.findNodeList(LABEL_CHILDREN)
                .map(JavaList::size)
                .orElse(0);
    }

    private static Result<Node, CompileError> pass(Node root) {
        return beforeNode(root).orElse(new Ok<>(root))
                .flatMapValue(Compiler::passNodes)
                .flatMapValue(Compiler::passNodeLists)
                .flatMapValue(node -> afterNode(node).orElse(new Ok<>(node)));
    }

    private static Option<Result<Node, CompileError>> beforeNode(Node node) {
        return new None<>();
    }

    private static Result<Node, CompileError> passNodeLists(Node withNodes) {
        return withNodes.streamNodeLists().foldLeftToResult(withNodes, (node, tuple) -> {
            final var propertyKey = tuple.left();
            final var propertyValues = tuple.right();
            return propertyValues.stream()
                    .map(Compiler::pass)
                    .into(ResultStream::new)
                    .foldResultsLeft(new JavaList<Node>(), JavaList::add)
                    .mapValue(newValues -> node.withNodeList(propertyKey, newValues));
        });
    }

    private static Result<Node, CompileError> passNodes(Node root) {
        return root.streamNodes().foldLeftToResult(root, (node, tuple) -> {
            final var propertyKey = tuple.left();
            final var propertyValue = tuple.right();
            return pass(propertyValue).mapValue(newValue -> node.withNode(propertyKey, newValue));
        });
    }

    private static Option<Result<Node, CompileError>> afterNode(Node node) {
        return new None<>();
    }
}