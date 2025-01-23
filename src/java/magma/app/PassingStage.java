package magma.app;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.error.CompileError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static magma.app.lang.CommonLang.CONTENT_AFTER_CHILDREN;
import static magma.app.lang.CommonLang.CONTENT_BEFORE_CHILD;
import static magma.app.lang.CommonLang.CONTENT_CHILDREN;
import static magma.app.lang.CommonLang.GENERIC_CHILDREN;
import static magma.app.lang.CommonLang.GENERIC_PARENT;
import static magma.app.lang.CommonLang.GENERIC_TYPE;
import static magma.app.lang.CommonLang.METHOD_DEFINITION;
import static magma.app.lang.CommonLang.METHOD_TYPE;
import static magma.app.lang.CommonLang.METHOD_VALUE;

public class PassingStage {
    public static Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit) {
        return beforePass(unit)
                .flatMapValue(PassingStage::passNodes)
                .flatMapValue(PassingStage::passNodeLists)
                .flatMapValue(PassingStage::afterPass);
    }

    private static Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit.filter(by("block")).map(PassUnit::enter).map(inner -> inner.mapValue(PassingStage::removeWhitespace))
                .or(() -> unit.filterAndMapToValue(by(GENERIC_TYPE), PassingStage::replaceWithFunctional))
                .or(() -> unit.filterAndMapToValue(by("construction"), PassingStage::replaceWithInvocation))
                .orElse(unit));
    }

    private static Node replaceWithInvocation(Node node) {
        final var type = node.findString("type").orElse("");
        final var symbol = new MapNode("symbol").withString("value", type);
        return node.retype("invocation")
                .withNode("caller", new MapNode("data-access")
                        .withNode("ref", symbol)
                        .withString("property", "new"));
    }

    private static Node replaceWithFunctional(Node generic) {
        final var parent = generic.findString(GENERIC_PARENT).orElse("");
        final var children = generic.findNodeList(GENERIC_CHILDREN).orElse(Collections.emptyList());

        if (parent.equals("Supplier")) {
            return new MapNode("functional").withNode("return", children.get(0));
        }
        if (parent.equals("Function")) {
            return new MapNode("functional")
                    .withNodeList("params", List.of(children.get(0)))
                    .withNode("return", children.get(1));
        }
        return generic;
    }

    private static Node removeWhitespace(Node block) {
        return block.mapNodeList(CONTENT_CHILDREN, children -> {
            return children.stream()
                    .filter(child -> !child.is("whitespace"))
                    .toList();
        });
    }

    private static Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(by("root"), PassingStage::removePackageStatements)
                .or(() -> unit.filterAndMapToValue(by("class").or(by("record")).or(by("interface")), PassingStage::retypeToStruct))
                .or(() -> unit.filterAndMapToValue(by("definition"), PassingStage::pruneDefinition))
                .or(() -> unit.filter(by("block")).map(PassingStage::formatBlock))
                .orElse(unit));
    }

    private static PassUnit<Node> formatBlock(PassUnit<Node> inner) {
        return inner.exit().flattenNode((State state, Node block) -> {
            if (block.findNodeList(CONTENT_CHILDREN).orElse(new ArrayList<>()).isEmpty()) {
                return block.removeNodeList(CONTENT_CHILDREN);
            }

            return block
                    .withString(CONTENT_AFTER_CHILDREN, formatIndent(state.depth()))
                    .mapNodeList(CONTENT_CHILDREN, children -> children.stream()
                            .map(child -> child.withString(CONTENT_BEFORE_CHILD, formatIndent(state.depth() + 1)))
                            .toList());
        });
    }

    private static String formatIndent(int state) {
        return "\n" + "\t".repeat(state);
    }

    private static Node pruneDefinition(Node definition) {
        return definition
                .removeNodeList("annotations")
                .removeNodeList("modifiers");
    }

    private static Node retypeToStruct(Node node) {
        final var name = node.findString("name").orElse("");
        return node.retype("struct").mapNode("value", value -> {
            return value.mapNodeList(CONTENT_CHILDREN, children -> {
                final var thisType = new MapNode("struct")
                        .withString("value", name);
                final var children1 = new ArrayList<>(children);
                final var propertyValue = new MapNode("block").withNodeList(CONTENT_CHILDREN, List.of(
                        new MapNode("definition")
                                .withNode("type", thisType)
                                .withString("name", "this"),
                new MapNode("return").withNode("value", new MapNode("symbol").withString("value", "this"))));
                children1.add(new MapNode(METHOD_TYPE)
                        .withNode(METHOD_DEFINITION, new MapNode("definition")
                                .withString("name", "new")
                                .withNode("type", thisType))
                        .withNode(METHOD_VALUE, propertyValue));
                return children1;
            });
        });
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