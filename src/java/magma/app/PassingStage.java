package magma.app;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.error.CompileError;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static magma.app.lang.CommonLang.CONTENT_AFTER_CHILDREN;
import static magma.app.lang.CommonLang.CONTENT_BEFORE_CHILD;
import static magma.app.lang.CommonLang.CONTENT_CHILDREN;

public class PassingStage {
    public static Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit) {
        return beforePass(unit)
                .flatMapValue(PassingStage::passNodes)
                .flatMapValue(PassingStage::passNodeLists)
                .flatMapValue(PassingStage::afterPass);
    }

    private static Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit.filter(by("block")).map(PassUnit::enter).map(inner -> inner.mapValue(block -> {
                    return block.mapNodeList(CONTENT_CHILDREN, children -> {
                        return children.stream()
                                .filter(child -> !child.is("whitespace"))
                                .toList();
                    });
                }))
                .orElse(unit));
    }

    private static Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(by("root"), PassingStage::removePackageStatements)
                .or(() -> unit.filterAndMapToValue(by("class").or(by("record")).or(by("interface")), PassingStage::retypeToStruct))
                .or(() -> unit.filterAndMapToValue(by("definition"), PassingStage::pruneDefinition))
                .or(() -> unit.filter(by("block"))
                        .map(PassingStage::formatBlock))
                .orElse(unit));
    }

    private static PassUnit<Node> formatBlock(PassUnit<Node> inner) {
        return inner.exit().flattenNode((State state, Node block) -> {
            if(block.findNodeList(CONTENT_CHILDREN).orElse(new ArrayList<>()).isEmpty()) {
                return block.removeNodeList(CONTENT_CHILDREN);
            }

            return block
                    .withString(CONTENT_AFTER_CHILDREN, "\n")
                    .mapNodeList(CONTENT_CHILDREN, children -> children.stream()
                    .map(child -> child.withString(CONTENT_BEFORE_CHILD, "\n" + "\t".repeat(state.depth() + 1)))
                    .toList());
        });
    }

    private static Node pruneDefinition(Node definition) {
        return definition
                .removeNodeList("annotations")
                .removeNodeList("modifiers");
    }

    private static Node retypeToStruct(Node node) {
        return node.retype("struct");
    }

    private static Node removePackageStatements(Node root) {
        return root.mapNodeList(CONTENT_CHILDREN, children -> children.stream()
                .filter(child -> !child.is("package"))
                .filter(PassingStage::filterImport)
                .toList());
    }

    private static boolean filterImport(Node child) {
        if (!child.is("import")) return true;

        final var namespace = child.findString("namespace").orElse("");
        return !namespace.startsWith("java.util.function");
    }

    private static Predicate<Node> by(String type) {
        return node -> node.is(type);
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

    private static List<Node> add(PassUnit<List<Node>> unit2, Node value) {
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