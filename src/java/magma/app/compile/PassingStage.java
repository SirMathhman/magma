package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.java.JavaLists;
import magma.java.JavaStreams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static magma.app.compile.lang.CommonLang.FUNCTION_TYPE;
import static magma.app.compile.lang.MagmaLang.ROOT_TYPE;

public class PassingStage {
    static Result<Node, CompileError> pass(Node node) {
        return passNodes(node)
                .flatMapValue(PassingStage::passNodeLists)
                .flatMapValue(node1 -> createPasser().beforePass(node1).orElse(new Ok<>(node1)));
    }

    private static Passer createPasser() {
        return new CompoundPasser(Collections.singletonList(new Passer() {
            @Override
            public Option<Result<Node, CompileError>> afterPass(Node node) {
                if (!node.is(ROOT_TYPE)) return new None<>();
                return new Some<>(new Ok<>(node.retype(FUNCTION_TYPE).orElse(node)));
            }
        }));
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
