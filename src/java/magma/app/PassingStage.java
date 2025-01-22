package magma.app;

import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.error.CompileError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static magma.app.lang.CommonLang.BLOCK_AFTER_CHILDREN;
import static magma.app.lang.CommonLang.CONTENT_AFTER_CHILD;
import static magma.app.lang.CommonLang.CONTENT_BEFORE_CHILD;
import static magma.app.lang.CommonLang.METHOD_CHILD;
import static magma.app.lang.CommonLang.METHOD_TYPE;
import static magma.app.lang.CommonLang.STRUCT_AFTER_CHILDREN;

public class PassingStage {
    public static Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit) {
        return beforePass(unit)
                .flatMapValue(PassingStage::passNodes)
                .flatMapValue(PassingStage::passNodeLists)
                .flatMapValue(PassingStage::afterPass);
    }

    private static Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return unit.filterAndMapToValue(by("root"), PassingStage::removePackageStatements).<Result<PassUnit<Node>, CompileError>>map(Ok::new)
                .or(() -> unit.filterAndMapToValue(by("class").or(by("interface").or(by("record"))), PassingStage::renameToStruct).map(Ok::new))
                .or(() -> unit.filterAndMapToValue(by("array"), PassingStage::renameToSlice).map(Ok::new))
                .or(() -> unit.filterAndMapToValue(by("method-access"), PassingStage::renameToDataAccess).map(Ok::new))
                .or(() -> unit.filterAndMapToValue(by("lambda"), PassingStage::renameLambdaToMethod).map(Ok::new))
                .or(() -> enterBlock(unit))
                .orElse(new Ok<>(unit));
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> enterBlock(PassUnit<Node> unit) {
        return unit.filter(by("block"))
                .map(PassUnit::enter)
                .map(Ok::new);
    }

    private static Predicate<Node> by(String type) {
        return node -> node.is(type);
    }

    private static Node renameLambdaToMethod(Node root) {
        final var args = findArgumentValue(root)
                .or(() -> findArgumentValues(root))
                .orElse(new ArrayList<>())
                .stream()
                .map(PassingStage::wrapUsingAutoType)
                .toList();

        final var value = root.findNode("child").orElse(new MapNode());

        final var propertyValue = value.is("block") ? value : new MapNode("block").withNodeList("children", List.of(
                new MapNode("return").withNode("value", value)
        ));

        final var retyped = root.retype(METHOD_TYPE);
        final var params = args.isEmpty() ? retyped : retyped.withNodeList("params", args);

        final var definition = new MapNode("definition")
                .withString("name", Namer.createUniqueName())
                .withNode("type", createAutoType());

        return params.withNode(METHOD_CHILD, propertyValue).withNode("definition", definition);
    }

    private static Node renameToDataAccess(Node node) {
        return node.retype("data-access");
    }

    private static Node renameToSlice(Node node) {
        final var child = node.findNode("child").orElse(new MapNode());
        return new MapNode("slice").withNode("child", child);
    }

    private static Node renameToStruct(Node node) {
        return node.retype("struct")
                .withString(STRUCT_AFTER_CHILDREN, "\n");
    }

    private static Node removePackageStatements(Node node) {
        return node.mapNodeList("children", PassingStage::removePackages);
    }

    private static Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return unit.filterAndMapToValue(by("definition"), PassingStage::cleanupDefinition).<Result<PassUnit<Node>, CompileError>>map(Ok::new)
                .or(() -> unit.filterAndMapToValue(by("root"), PassingStage::formatRoot).map(Ok::new))
                .or(() -> formatBlock(unit))
                .or(() -> pruneAndFormatStruct(unit))
                .or(() -> unit.filterAndMapToValue(by("method"), PassingStage::pruneFunction).map(Ok::new))
                .orElse(new Ok<>(unit));
    }

    private static Node pruneFunction(Node node) {
        return node.mapNode("definition", definition -> definition.removeNodeList("annotations"));
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> pruneAndFormatStruct(PassUnit<Node> unit) {
        return unit.filterAndMapToCached(by("struct"), PassingStage::pruneStruct)
                .map(pruned -> pruned.flattenNode(PassingStage::formatContent))
                .map(Ok::new);
    }

    private static Tuple<List<Node>, Node> pruneStruct(Node value1) {
        final var pruned = pruneModifiers(value1);
        final var children = pruned.findNodeList("children").orElse(new ArrayList<>());
        final var methods = new ArrayList<Node>();
        final var newChildren = new ArrayList<Node>();
        children.forEach(child -> {
            if (child.is("method")) {
                methods.add(child);
            } else {
                newChildren.add(child);
            }
        });

        final var newFunction = newChildren.isEmpty()
                ? pruned.removeNodeList("children")
                : pruned.withNodeList("children", newChildren);

        return new Tuple<>(methods, newFunction);
    }

    private static Node formatContent(State state, Node value) {
        final var propertyValue = formatIndent(Math.max(state.depth() - 1, 0));
        return value.withString(BLOCK_AFTER_CHILDREN, propertyValue)
                .mapNodeList("children", children -> formatChild(children, state.depth()));
    }

    private static List<Node> formatChild(List<Node> children, int depth) {
        return children.stream()
                .map(child -> child.withString(CONTENT_BEFORE_CHILD, formatIndent(depth)))
                .toList();
    }

    private static String formatIndent(int depth) {
        return "\n" + "\t".repeat(depth);
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> formatBlock(PassUnit<Node> unit) {
        return unit.filter(by("block"))
                .map(inner -> inner.flattenNode(PassingStage::formatContent))
                .map(PassUnit::exit)
                .map(Ok::new);
    }

    private static Node formatRoot(Node node) {
        return node.mapNode("definition", definition -> definition.mapNodeList("children", PassingStage::indentRootChildren));
    }

    private static Node cleanupDefinition(Node node) {
        return pruneModifiers(node)
                .mapNodeList("modifiers", PassingStage::replaceFinalWithConst)
                .mapNode("type", PassingStage::replaceVarWithAuto);
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

    private static Node wrapUsingAutoType(String name) {
        return new MapNode("definition")
                .withString("name", name)
                .withNode("type", createAutoType());
    }

    private static Optional<List<String>> findArgumentValue(Node node) {
        return node.findNode("arg")
                .flatMap(child -> child.findString("value"))
                .map(Collections::singletonList);
    }

    private static Optional<List<String>> findArgumentValues(Node node) {
        return node.findNodeList("args").map(list -> list.stream()
                .map(child -> child.findString("value"))
                .flatMap(Optional::stream)
                .toList());
    }

    private static Node createAutoType() {
        return new MapNode("symbol").withString("value", "auto");
    }

    private static List<Node> replaceFinalWithConst(List<Node> modifiers) {
        return modifiers.stream()
                .map(child -> child.findString("value"))
                .flatMap(Optional::stream)
                .map(modifier -> modifier.equals("final") ? "const" : modifier)
                .map(value -> new MapNode("modifier").withString("value", value))
                .toList();
    }

    private static Node replaceVarWithAuto(Node type) {
        if (!type.is("symbol")) return type;

        final var value = type.findString("value").orElse("");
        if (!value.equals("var")) return type;
        return createAutoType();
    }

    private static Node pruneModifiers(Node node) {
        final var modifiers = node.findNodeList("modifiers").orElse(Collections.emptyList());
        final var newModifiers = modifiers.stream()
                .map(modifier -> modifier.findString("value"))
                .flatMap(Optional::stream)
                .filter(modifier -> !modifier.equals("public") && !modifier.equals("private"))
                .map(modifier -> new MapNode("modifier").withString("value", modifier))
                .toList();

        if (newModifiers.isEmpty()) {
            return node.removeNodeList("modifiers");
        } else {
            return node.withNodeList("modifiers", newModifiers);
        }
    }

    private static List<Node> removePackages(List<Node> children) {
        return children.stream()
                .filter(child -> !child.is("package"))
                .toList();
    }

    private static List<Node> indentRootChildren(List<Node> rootChildren) {
        return rootChildren.stream()
                .map(child -> child.withString(CONTENT_AFTER_CHILD, "\n"))
                .toList();
    }

}