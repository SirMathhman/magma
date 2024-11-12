package magma.app.compile;

import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.java.JavaStreams;

import java.util.ArrayList;
import java.util.List;

public class TreePassingStage implements PassingStage {
    private final Passer passer;

    public TreePassingStage(Passer passer) {
        this.passer = passer;
    }

    private Result<Tuple<State, Node>, CompileError> passNodeLists(Tuple<State, Node> node) {
        return node.right().streamNodeLists().foldLeftToResult(node, this::passNodeList);
    }

    private Result<Tuple<State, Node>, CompileError> passNodeList(Tuple<State, Node> tuple, Tuple<String, List<Node>> entry) {
        final var propertyKey = entry.left();
        final var propertyValues = entry.right();
        final var oldState = tuple.left();
        final var oldRoot = tuple.right();
        return JavaStreams.fromList(propertyValues)
                .foldLeftToResult(new Tuple<>(oldState, new ArrayList<>()), this::passNodeWithinList)
                .mapValue(list -> list.mapRight(right -> oldRoot.withNodeList(propertyKey, right)));
    }

    private Result<Tuple<State, List<Node>>, CompileError> passNodeWithinList(Tuple<State, List<Node>> tuple, Node child) {
        final var state = tuple.left();
        final var newValues = tuple.right();
        return pass(state, child).mapValue(passed -> passed.mapRight(right -> {
            final var copy = new ArrayList<>(newValues);
            copy.add(right);
            return copy;
        }));
    }

    @Override
    public Result<Tuple<State, Node>, CompileError> pass(State state, Node node) {
        final var beforePassed = passer.beforePass(state, node).orElse(new Ok<>(new Tuple<>(state, node)));
        return beforePassed.flatMapValue(this::passNodes)
                .flatMapValue(this::passNodeLists)
                .flatMapValue(passed -> passer.afterPass(passed.left(), passed.right()).orElse(new Ok<>(passed)));
    }

    private Result<Tuple<State, Node>, CompileError> passNodes(Tuple<State, Node> initial) {
        return initial.right().streamNodes().foldLeftToResult(initial, (current, tuple) -> {
            final var oldState = current.left();
            final var folded = current.right();

            final var propertyKey = tuple.left();
            final var oldValue = tuple.right();
            return pass(oldState, oldValue).mapValue(result -> result.mapRight(newValue -> folded.withNode(propertyKey, newValue)));
        });
    }
}
