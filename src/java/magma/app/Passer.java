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
import static magma.app.lang.CommonLang.STRUCT_AFTER_CHILDREN;

public class Passer {
    public static Result<Tuple<State, Node>, CompileError> pass(State state, Node root) {
        return beforePass(state, root).orElse(new Ok<>(new Tuple<>(state, root)))
                .flatMapValue(passedBefore -> passNodes(passedBefore.left(), passedBefore.right()))
                .flatMapValue(passedNodes -> passNodeLists(passedNodes.left(), passedNodes.right()))
                .flatMapValue(passedNodeLists -> afterPass(passedNodeLists.left(), passedNodeLists.right()).orElse(new Ok<>(new Tuple<>(passedNodeLists.left(), passedNodeLists.right()))));
    }

    public static Optional<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        return removePackageStatements(state, node)
                .or(() -> renameToStruct(state, node));
    }

    private static Optional<Result<Tuple<State, Node>, CompileError>> renameToStruct(State state, Node node) {
        if (node.is("class") || node.is("interface") || node.is("record")) {
            return Optional.of(new Ok<>(new Tuple<>(state, node
                    .retype("struct")
                    .withString(STRUCT_AFTER_CHILDREN, "\n"))));
        }

        if (node.is("block")) {
            return Optional.of(new Ok<>(new Tuple<>(state.enter(), node)));
        }

        return Optional.empty();
    }

    private static Optional<Result<Tuple<State, Node>, CompileError>> removePackageStatements(State state, Node node) {
        if (!node.is("root")) {
            return Optional.empty();
        }

        final var node1 = node.mapNodeList("children", children -> {
            return children.stream()
                    .filter(child -> !child.is("package"))
                    .toList();
        });

        return Optional.of(new Ok<>(new Tuple<>(state, node1)));
    }

    public static Result<Tuple<State, Node>, CompileError> passNodeLists(State state, Node previous) {
        return previous.streamNodeLists()
                .foldLeftToResult(new Tuple<>(state, previous),
                        (current, tuple) -> passNodeList(current.left(), current.right(), tuple));
    }

    public static Result<Tuple<State, Node>, CompileError> passNodeList(
            State state,
            Node root,
            Tuple<String, List<Node>> pair
    ) {
        final var propertyKey = pair.left();
        final var propertyValues = pair.right();
        return passNodeListInStream(state, propertyValues)
                .mapValue(list -> list.mapRight(right -> root.withNodeList(propertyKey, right)));
    }

    private static Result<Tuple<State, List<Node>>, CompileError> passNodeListInStream(State state, List<Node> elements) {
        return Streams.from(elements).foldLeftToResult(new Tuple<>(state, new ArrayList<>()), (current, currentElement) -> {
            final var currentState = current.left();
            final var currentElements = current.right();

            return pass(currentState, currentElement).mapValue(passingResult -> {
                return passingResult.mapRight(passedElement -> {
                    final var copy = new ArrayList<>(currentElements);
                    copy.add(passedElement);
                    return copy;
                });
            });
        });
    }

    public static Optional<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (node.is("root")) {
            final var newNode = node.mapNodeList("children", children -> {
                return children.stream()
                        .map(child -> child.withString(CONTENT_AFTER_CHILD, "\n"))
                        .toList();
            });
            return Optional.of(new Ok<>(new Tuple<>(state, newNode)));
        }

        if (node.is("block")) {
            return Optional.of(new Ok<>(new Tuple<>(state.exit(), getChildren(state, node))));
        }

        if (node.is("struct")) {
            return Optional.of(new Ok<>(new Tuple<>(state, getChildren(state, node))));
        }

        return Optional.empty();
    }

    private static Node getChildren(State state, Node node) {
        return node.withString(BLOCK_AFTER_CHILDREN, "\n" + "\t".repeat(state.depth())).mapNodeList("children", children -> {
            return children.stream()
                    .map(child -> child.withString(CONTENT_BEFORE_CHILD, "\n" + "\t".repeat(state.depth() + 1)))
                    .toList();
        });
    }

    public static Result<Tuple<State, Node>, CompileError> passNodes(State state, Node root) {
        return root.streamNodes().foldLeftToResult(new Tuple<>(state, root), Passer::foldNode);
    }

    private static Result<Tuple<State, Node>, CompileError> foldNode(
            Tuple<State, Node> current,
            Tuple<String, Node> tuple
    ) {
        final var currentState = current.left();
        final var currentRoot = current.right();
        final var pairKey = tuple.left();
        final var pairNode = tuple.right();

        return pass(currentState, pairNode).mapValue(passed -> passed.mapRight(right -> currentRoot.withNode(pairKey, right)));
    }
}