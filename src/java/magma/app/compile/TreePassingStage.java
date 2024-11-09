package magma.app.compile;

import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.java.JavaList;
import magma.java.JavaStreams;

import java.util.ArrayList;
import java.util.List;

public class TreePassingStage implements PassingStage {
    private final Passer passer;

    public TreePassingStage(Passer passer) {
        this.passer = passer;
    }

    private Result<Node, CompileError> passNodeLists(Node node) {
        return node.streamNodeLists().foldLeftToResult(node, this::foldNodeListsIntoNode);
    }

    private Result<Node, CompileError> foldNodeListsIntoNode(Node node, Tuple<String, List<Node>> entry) {
        return JavaStreams.fromList(entry.right())
                .foldLeftToResult(new ArrayList<>(), this::passAndAnd)
                .mapValue(list -> node.withNodeList(entry.left(), list));
    }

    private Result<List<Node>, CompileError> passAndAnd(List<Node> values, Node value) {
        return pass(value).mapValue(passed -> {
            JavaList<Node> javaList = new JavaList<>(values);
            return javaList.add(passed).list();
        });
    }

    private Result<Node, CompileError> passNodes(Node node) {
        return node.streamNodes()
                .foldLeftToResult(node, (current, tuple) -> pass(tuple.right())
                        .mapValue(value -> current.withNode(tuple.left(), value)));
    }

    @Override
    public Result<Node, CompileError> pass(Node node) {
        final var beforePassed = passer.beforePass(node).orElse(new Ok<>(node));
        return beforePassed.flatMapValue(this::passNodes)
                .flatMapValue(this::passNodeLists)
                .flatMapValue(node1 -> passer.afterPass(node1).orElse(new Ok<>(node1)));
    }
}
