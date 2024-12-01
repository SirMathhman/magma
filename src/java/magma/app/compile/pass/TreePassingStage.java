package magma.app.compile.pass;

import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.java.JavaList;

public class TreePassingStage implements PassingStage {
    private final Passer passer;

    public TreePassingStage(Passer passer) {
        this.passer = passer;
    }

    public Result<Tuple<State, Node>, CompileError> passNodeLists(State state, Node root) {
        return root.streamNodeLists().foldLeftToResult(new Tuple<>(state, root), this::passNodeList);
    }

    private Result<Tuple<State, Node>, CompileError> passNodeList(
            Tuple<State, Node> current,
            Tuple<String, JavaList<Node>> next
    ) {
        final var state = current.left();
        final var node = current.right();
        final var propertyKey = next.left();
        final var propertyValues = next.right();

        return propertyValues.stream()
                .foldLeftToResult(new Tuple<>(state, new JavaList<Node>()), (tuple, node1) -> {
                    final var oldState = tuple.left();
                    final var oldList = tuple.right();

                    return pass(oldState, node1).mapValue(inner -> inner.mapRight(oldList::add));
                })
                .mapValue(tuple -> tuple.mapRight(newValues -> node.withNodeList(propertyKey, newValues)));
    }

    public Result<Tuple<State, Node>, CompileError> passNodes(State state, Node root) {
        return root.streamNodes().foldLeftToResult(new Tuple<>(state, root), (current, next) -> {
            final var oldState = current.left();
            final var oldNode = current.right();
            final var propertyKey = next.left();
            final var propertyValue = next.right();
            return pass(oldState, propertyValue).mapValue(
                    newPair -> newPair.mapRight(
                            right -> oldNode.withNode(propertyKey, right)));
        });
    }

    @Override
    public Result<Tuple<State, Node>, CompileError> pass(State state, Node root) {
        return passer.beforePass(state, root).orElse(new Ok<>(new Tuple<>(state, root)))
                .flatMapValue(before -> passNodes(before.left(), before.right()))
                .flatMapValue(withNodes -> passNodeLists(withNodes.left(), withNodes.right()))
                .flatMapValue(withNodes -> passer.afterPass(withNodes.left(), withNodes.right()).orElse(new Ok<>(withNodes)));
    }
}