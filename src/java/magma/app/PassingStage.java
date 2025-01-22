package magma.app;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.error.CompileError;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PassingStage {
    public static Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit) {
        return beforePass(unit)
                .flatMapValue(PassingStage::passNodes)
                .flatMapValue(PassingStage::passNodeLists)
                .flatMapValue(PassingStage::afterPass);
    }

    private static Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit);
    }

    private static Predicate<Node> by(String type) {
        return node -> node.is(type);
    }

    private static Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit);
    }

    private static Result<PassUnit<Node>, CompileError> passNodeLists(PassUnit<Node> unit) {
        return unit.value().streamNodeLists().foldLeftToResult(unit, (current, tuple) -> {
            final var propertyKey = tuple.left();
            final var propertyValues = tuple.right();
            return Streams.from(propertyValues)
                    .foldLeftToResult(current.withValue(new ArrayList<>()), PassingStage::passAndAdd)
                    .mapValue(unit1 -> unit1.mapValue(node -> current.value().withNodeList(propertyKey, node)));
        });
    }

    private static Result<PassUnit<List<Node>>, CompileError> passAndAdd(
            PassUnit<List<Node>> unit,
            Node element
    ) {
        return pass(unit.withValue(element)).mapValue(result -> result.mapValue(value -> add(unit, value)));
    }

    private static ArrayList<Node> add(PassUnit<List<Node>> unit2, Node value) {
        final var copy = new ArrayList<>(unit2.value());
        copy.add(value);
        return copy;
    }

    private static Result<PassUnit<Node>, CompileError> passNodes(PassUnit<Node> unit) {
        return unit.value().streamNodes().foldLeftToResult(unit, (current, tuple) -> {
            final var pairKey = tuple.left();
            final var pairNode = tuple.right();

            return pass(current.withValue(pairNode))
                    .mapValue(passed -> passed.mapValue(value -> current.value().withNode(pairKey, value)));
        });
    }
}