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
import java.util.function.Function;

import static magma.app.lang.CommonLang.BLOCK_AFTER_CHILDREN;
import static magma.app.lang.CommonLang.CONTENT_AFTER_CHILD;
import static magma.app.lang.CommonLang.CONTENT_BEFORE_CHILD;
import static magma.app.lang.CommonLang.METHOD_CHILD;
import static magma.app.lang.CommonLang.METHOD_TYPE;
import static magma.app.lang.CommonLang.STRUCT_AFTER_CHILDREN;

public class Passer {
    private static int counter = 0;

    public static Result<PassUnit<Node>, CompileError> pass(State state, Node root) {
        return beforePass(new PassUnit<>(state, root))
                .flatMapValue(Passer::passNodes)
                .flatMapValue(Passer::passNodeLists)
                .flatMapValue(Passer::afterPass);
    }

    private static Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return removePackageStatements(unit)
                .or(() -> renameToStruct(unit))
                .or(() -> renameToSlice(unit))
                .or(() -> renameToDataAccess(unit))
                .or(() -> renameLambdaToMethod(unit))
                .or(() -> enterBlock(unit))
                .orElse(new Ok<>(unit));
    }

    private static Optional<? extends Result<PassUnit<Node>, CompileError>> enterBlock(PassUnit<Node> unit) {
        Node node = unit.right();
        if (!node.is("block")) return Optional.empty();

        return Optional.of(new Ok<>(new PassUnit<>(unit.left().enter(), node)));
    }

    private static Optional<? extends Result<PassUnit<Node>, CompileError>> renameLambdaToMethod(PassUnit<Node> unit) {
        Node node = unit.right();
        if (!node.is("lambda")) return Optional.empty();

        final var args = findArgumentValue(node)
                .or(() -> findArgumentValues(node))
                .orElse(new ArrayList<>())
                .stream()
                .map(Passer::wrapUsingAutoType)
                .toList();

        final var value = node.findNode("child").orElse(new MapNode());

        final var propertyValue = value.is("block") ? value : new MapNode("block").withNodeList("children", List.of(
                new MapNode("return").withNode("value", value)
        ));

        final var retyped = node.retype(METHOD_TYPE);
        final var params = args.isEmpty() ? retyped : retyped.withNodeList("params", args);

        final var definition = new MapNode("definition")
                .withString("name", createUniqueName())
                .withNode("type", createAutoType());

        final var method = params
                .withNode(METHOD_CHILD, propertyValue)
                .withNode("definition", definition);

        return Optional.of(new Ok<>(new PassUnit<>(unit.left(), method)));
    }

    private static Optional<? extends Result<PassUnit<Node>, CompileError>> renameToDataAccess(PassUnit<Node> unit) {
        Node node = unit.right();
        if (!node.is("method-access")) return Optional.empty();

        return Optional.of(new Ok<>(new PassUnit<>(unit.left(), node.retype("data-access"))));
    }

    private static Optional<? extends Result<PassUnit<Node>, CompileError>> renameToSlice(PassUnit<Node> unit) {
        Node node = unit.right();
        if (!node.is("array")) return Optional.empty();

        final var child = node.findNode("child").orElse(new MapNode());
        final var slice = new MapNode("slice").withNode("child", child);

        return Optional.of(new Ok<>(new PassUnit<>(unit.left(), slice)));
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> renameToStruct(PassUnit<Node> unit) {
        Node node = unit.right();
        if (!node.is("class") && !node.is("interface") && !node.is("record")) return Optional.empty();

        return Optional.of(new Ok<>(new PassUnit<>(unit.left(), node
                .retype("struct")
                .withString(STRUCT_AFTER_CHILDREN, "\n"))));

    }

    private static Optional<Result<PassUnit<Node>, CompileError>> removePackageStatements(PassUnit<Node> unit) {
        Node node = unit.right();
        if (!node.is("root")) {
            return Optional.empty();
        }

        final var node1 = node.mapNodeList("children", Passer::removePackages);
        return Optional.of(new Ok<>(new PassUnit<>(unit.left(), node1)));
    }

    private static Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return removeAccessModifiersFromDefinitions(unit)
                .or(() -> formatRoot(unit))
                .or(() -> formatBlock(unit))
                .or(() -> pruneAndFormatStruct(unit))
                .or(() -> pruneFunction(unit))
                .orElse(new Ok<>(unit));
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> pruneFunction(PassUnit<Node> unit) {
        return filter(unit.left(), unit.right(), "method", node1 -> node1.mapNode("definition", definition -> definition.removeNodeList("annotations")));
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> pruneAndFormatStruct(PassUnit<Node> unit) {
        State state = unit.left();
        Node node = unit.right();
        if (!node.is("struct")) return Optional.empty();

        final var pruned = pruneModifiers(node);
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

        final var withNewChildren = newChildren.isEmpty()
                ? pruned.removeNodeList("children")
                : pruned.withNodeList("children", newChildren);

        final var wrapped = new MapNode("wrap")
                .withNodeList("children", methods)
                .withNode("value", withNewChildren);

        return Optional.of(new Ok<>(new PassUnit<>(state, formatContent(state, wrapped))));
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> formatBlock(PassUnit<Node> unit) {
        State state = unit.left();
        Node node = unit.right();
        if (!node.is("block")) return Optional.empty();

        return Optional.of(new Ok<>(new PassUnit<>(state.exit(), formatContent(state, node))));
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> formatRoot(PassUnit<Node> unit) {
        return filter(unit.left(), unit.right(), "root", node1 -> node1.mapNode("definition", definition -> definition.mapNodeList("children", Passer::indentRootChildren)));
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> removeAccessModifiersFromDefinitions(PassUnit<Node> unit) {
        Node node = unit.right();
        if (!node.is("definition")) return Optional.empty();

        final var newNode = pruneModifiers(node)
                .mapNodeList("modifiers", Passer::replaceFinalWithConst)
                .mapNode("type", Passer::replaceVarWithAuto);

        return Optional.of(new Ok<>(new PassUnit<>(unit.left(), newNode)));
    }

    private static Result<PassUnit<Node>, CompileError> passNodeLists(PassUnit<Node> passedNodes) {
        return passNodeLists(passedNodes.left(), passedNodes.right());
    }

    private static Result<PassUnit<Node>, CompileError> passNodes(PassUnit<Node> passedBefore) {
        Node root = passedBefore.right();
        return root.streamNodes().foldLeftToResult(passedBefore, Passer::foldNode);
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

    private static String createUniqueName() {
        final var name = "_lambda" + counter + "_";
        counter++;
        return name;
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

    public static Result<PassUnit<Node>, CompileError> passNodeLists(State state, Node previous) {
        return previous.streamNodeLists()
                .foldLeftToResult(new PassUnit<>(state, previous),
                        (current, tuple) -> passNodeList(current.left(), current.right(), tuple));
    }

    public static Result<PassUnit<Node>, CompileError> passNodeList(
            State state,
            Node root,
            Tuple<String, List<Node>> pair
    ) {
        final var propertyKey = pair.left();
        final var propertyValues = pair.right();
        return passNodeListInStream(state, propertyValues)
                .mapValue(list -> new PassUnit<>(list.left(), root.withNodeList(propertyKey, list.right())));
    }

    private static Result<PassUnit<List<Node>>, CompileError> passNodeListInStream(State state, List<Node> elements) {
        return Streams.from(elements).foldLeftToResult(new PassUnit<>(state, new ArrayList<>()), (current, currentElement) -> {
            final var currentState = current.left();
            final var currentElements = current.right();
            return passAndFoldElementIntoList(currentElements, currentState, currentElement);
        });
    }

    private static Result<PassUnit<List<Node>>, CompileError> passAndFoldElementIntoList(
            List<Node> elements,
            State currentState,
            Node currentElement
    ) {
        return pass(currentState, currentElement).mapValue(result -> {
            final var copy = new ArrayList<>(elements);
            copy.add(result.right());
            return result.withValue(copy);
        });
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> filter(
            State state,
            Node node,
            String type,
            Function<Node, Node> mapper
    ) {
        if (!node.is(type)) return Optional.empty();

        final var node1 = mapper.apply(node);
        return Optional.of(new Ok<>(new PassUnit<>(state, node1)));
    }

    private static List<Node> indentRootChildren(List<Node> rootChildren) {
        return rootChildren.stream()
                .map(child -> child.withString(CONTENT_AFTER_CHILD, "\n"))
                .toList();
    }

    private static Node formatContent(State state, Node node) {
        return node.withString(BLOCK_AFTER_CHILDREN, "\n" + "\t".repeat(Math.max(state.depth() - 1, 0))).mapNodeList("children", children -> children.stream()
                .map(child -> child.withString(CONTENT_BEFORE_CHILD, "\n" + "\t".repeat(state.depth())))
                .toList());
    }

    private static Result<PassUnit<Node>, CompileError> foldNode(
            PassUnit<Node> current,
            Tuple<String, Node> tuple
    ) {
        final var currentState = current.left();
        final var currentRoot = current.right();
        final var pairKey = tuple.left();
        final var pairNode = tuple.right();

        return pass(currentState, pairNode).mapValue(passed -> new PassUnit<>(passed.left(), currentRoot.withNode(pairKey, passed.right())));
    }
}