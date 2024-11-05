package magma.app.compile;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.java.JavaLists;
import magma.java.JavaStreams;

import java.util.ArrayList;
import java.util.List;

public class PassingStage {
    static Result<Node, CompileError> pass(Node node) {
        return passNodes(node)
                .flatMapValue(PassingStage::passNodeLists)
                .flatMapValue(node1 -> new RootPasser().pass(node1));
    }

    private static Result<Node, CompileError> passNodeLists(Node node) {
        return node.streamNodeLists().foldLeftToResult(node, PassingStage::foldNodeListsIntoNode);
    }

    private static Result<Node, CompileError> foldNodeListsIntoNode(Node node, Tuple<String, List<Node>> entry) {
        return JavaStreams.fromList(entry.right())
                .foldLeftToResult(new ArrayList<>(), PassingStage::passAndAnd)
                .mapValue(list -> node.withNodeList(entry.left(), list));
    }

    private static Result<List<Node>, CompileError> passAndAnd(List<Node> values, Node value) {
        return pass(value).mapValue(passed -> JavaLists.add(values, passed));
    }

    private static Result<Node, CompileError> passNodes(Node node) {
        return node.streamNodes()
                .foldLeftToResult(node, (current, tuple) -> pass(tuple.right())
                        .mapValue(value -> current.withNode(tuple.left(), value)));
    }

}
