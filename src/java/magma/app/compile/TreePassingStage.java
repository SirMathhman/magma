package magma.app.compile;

import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.java.JavaLists;
import magma.java.JavaStreams;

import java.util.ArrayList;
import java.util.List;

public class TreePassingStage {
    private final Passer passer;

    public TreePassingStage(Passer passer) {
        this.passer = passer;
    }

    private static Result<Node, CompileError> passNodeLists(Node node) {
        return node.streamNodeLists().foldLeftToResult(node, TreePassingStage::foldNodeListsIntoNode);
    }

    private static Result<Node, CompileError> foldNodeListsIntoNode(Node node, Tuple<String, List<Node>> entry) {
        return JavaStreams.fromList(entry.right())
                .foldLeftToResult(new ArrayList<>(), TreePassingStage::passAndAnd)
                .mapValue(list -> node.withNodeList(entry.left(), list));
    }

    private static Result<List<Node>, CompileError> passAndAnd(List<Node> values, Node value) {
        return new TreePassingStage(Compiler.createPasser()).pass(value).mapValue(passed -> JavaLists.add(values, passed));
    }

    private static Result<Node, CompileError> passNodes(Node node) {
        return node.streamNodes()
                .foldLeftToResult(node, (current, tuple) -> new TreePassingStage(Compiler.createPasser()).pass(tuple.right())
                        .mapValue(value -> current.withNode(tuple.left(), value)));
    }

    Result<Node, CompileError> pass(Node node) {
        final var beforePassed = passer.beforePass(node).orElse(new Ok<>(node));
        return beforePassed.flatMapValue(TreePassingStage::passNodes)
                .flatMapValue(TreePassingStage::passNodeLists)
                .flatMapValue(node1 -> passer.afterPass(node1).orElse(new Ok<>(node1)));
    }
}
