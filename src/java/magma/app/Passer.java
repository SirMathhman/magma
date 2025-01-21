package magma.app;

import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.error.CompileError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static magma.app.lang.CommonLang.BLOCK_AFTER_CHILDREN;
import static magma.app.lang.CommonLang.CONTENT_AFTER_CHILD;
import static magma.app.lang.CommonLang.CONTENT_BEFORE_CHILD;

public class Passer {
    public static Result<Node, CompileError> pass(Node root) {
        return beforePass(root).orElse(new Ok<>(root))
                .flatMapValue(Passer::passNodes)
                .flatMapValue(Passer::passNodeLists)
                .flatMapValue(inner -> afterPass(inner).orElse(new Ok<>(inner)));
    }

    public static Optional<Result<Node, CompileError>> beforePass(Node node) {
        return removePackageStatements(node)
                .or(() -> renameToStruct(node));
    }

    private static Optional<? extends Result<Node, CompileError>> renameToStruct(Node node) {
        if (node.is("class") || node.is("interface") || node.is("record")) {
            return Optional.of(new Ok<>(node.retype("struct")));
        }

        return Optional.empty();
    }

    private static Optional<Result<Node, CompileError>> removePackageStatements(Node node) {
        if (!node.is("root")) {
            return Optional.empty();
        }
        final var node1 = node.mapNodeList("children", children -> {
            return children.stream()
                    .filter(child -> !child.is("package"))
                    .toList();
        });
        return Optional.of(new Ok<>(node1));
    }

    public static Result<Node, CompileError> passNodeLists(Node previous) {
        return previous.streamNodeLists().foldLeftToResult(previous, Passer::passNodeList);
    }

    public static Result<Node, CompileError> passNodeList(Node node, Tuple<String, List<Node>> tuple) {
        return Streams.from(tuple.right())
                .map(Passer::pass)
                .foldLeftToResult(new ArrayList<>(), Passer::foldElementIntoList)
                .mapValue(list -> node.withNodeList(tuple.left(), list));
    }

    public static Optional<Result<Node, CompileError>> afterPass(Node node) {
        if(node.is("root")) {
            return Optional.of(new Ok<>(node.mapNodeList("children", children -> {
                return children.stream()
                        .map(child -> child.withString(CONTENT_AFTER_CHILD, "\n"))
                        .toList();
            })));
        }

        if (node.is("block") || node.is("struct")) {
            return Optional.of(new Ok<>(node.withString(BLOCK_AFTER_CHILDREN, "\n").mapNodeList("children", children -> {
                return children.stream()
                        .map(child -> child.withString(CONTENT_BEFORE_CHILD, "\n\t"))
                        .toList();
            })));
        }
        return Optional.empty();
    }

    public static Result<List<Node>, CompileError> foldElementIntoList(List<Node> currentNodes, Result<Node, CompileError> node) {
        return node.mapValue(currentNewElement -> merge(currentNodes, currentNewElement));
    }

    private static List<Node> merge(List<Node> nodes, Node result) {
        final var copy = new ArrayList<>(nodes);
        copy.add(result);
        return copy;
    }

    public static Result<Node, CompileError> passNodes(Node root) {
        return root.streamNodes().foldLeftToResult(root, (node, tuple) -> pass(tuple.right()).mapValue(passed -> node.withNode(tuple.left(), passed)));
    }
}