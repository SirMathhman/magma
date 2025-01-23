package magma.app;

import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.error.CompileError;

import java.util.ArrayList;
import java.util.List;

public class PassingStage {
    static List<Node> add(PassUnit<List<Node>> unit2, Node value) {
        final var copy = new ArrayList<>(unit2.value());
        copy.add(value);
        return copy;
    }

    Result<PassUnit<List<Node>>, CompileError> passAndAdd(
            PassUnit<List<Node>> unit,
            Node element
    ) {
        return pass(unit.withValue(element)).mapValue(result -> result.mapValue(value -> add(unit, value)));
    }

    private Result<PassUnit<Node>, CompileError> passNodeLists(PassUnit<Node> unit) {
        return unit.value().streamNodeLists().foldLeftToResult(unit, (current, tuple) -> {
            final var propertyKey = tuple.left();
            final var propertyValues = tuple.right();
            return Streams.from(propertyValues)
                    .foldLeftToResult(current.withValue(new ArrayList<>()), this::passAndAdd)
                    .mapValue(unit1 -> unit1.mapValue(node -> current.value().withNodeList(propertyKey, node)));
        });
    }

    private Result<PassUnit<Node>, CompileError> passNodes(PassUnit<Node> unit) {
        return unit.value().streamNodes().foldLeftToResult(unit, (current, tuple) -> {
            final var pairKey = tuple.left();
            final var pairNode = tuple.right();

            return pass(current.withValue(pairNode))
                    .mapValue(passed -> passed.mapValue(value -> current.value().withNode(pairKey, value)));
        });
    }

    public Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit) {
        return Passer.beforePass(unit)
                .flatMapValue(this::passNodes)
                .flatMapValue(this::passNodeLists)
                .flatMapValue(Passer::afterPass);
    }
}