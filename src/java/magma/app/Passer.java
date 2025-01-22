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
        return beforePass(new InlinePassUnit<>(state, root))
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

    private static Optional<Result<PassUnit<Node>, CompileError>> enterBlock(PassUnit<Node> unit) {
        return unit.filter(node -> node.is("block"))
                .map(PassUnit::enter)
                .map(Ok::new);
    }

    private static Optional<? extends Result<PassUnit<Node>, CompileError>> renameLambdaToMethod(PassUnit<Node> unit) {
        Node node = unit.value();
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

        return Optional.of(new Ok<>(unit.withValue(method)));
    }

    private static Optional<? extends Result<PassUnit<Node>, CompileError>> renameToDataAccess(PassUnit<Node> unit) {
        Node node = unit.value();
        if (!node.is("method-access")) return Optional.empty();

        return Optional.of(new Ok<>(unit.withValue(node.retype("data-access"))));
    }

    private static Optional<? extends Result<PassUnit<Node>, CompileError>> renameToSlice(PassUnit<Node> unit) {
        Node node = unit.value();
        if (!node.is("array")) return Optional.empty();

        final var child = node.findNode("child").orElse(new MapNode());
        final var slice = new MapNode("slice").withNode("child", child);

        return Optional.of(new Ok<>(unit.withValue(slice)));
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> renameToStruct(PassUnit<Node> unit) {
        Node node = unit.value();
        if (!node.is("class") && !node.is("interface") && !node.is("record")) return Optional.empty();

        return Optional.of(new Ok<>(unit.withValue(node
                .retype("struct")
                .withString(STRUCT_AFTER_CHILDREN, "\n"))));

    }

    private static Optional<Result<PassUnit<Node>, CompileError>> removePackageStatements(PassUnit<Node> unit) {
        Node node = unit.value();
        if (!node.is("root")) {
            return Optional.empty();
        }

        final var node1 = node.mapNodeList("children", Passer::removePackages);
        return Optional.of(new Ok<>(unit.withValue(node1)));
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
        if (unit.value().is("method")) {
            final var node2 = unit.value().mapNode("definition", definition -> definition.removeNodeList("annotations"));
            return Optional.of(new Ok<>(unit.withValue(node2)));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> pruneAndFormatStruct(PassUnit<Node> unit) {
        if (!unit.value().is("struct")) return Optional.empty();

        final var pruned = pruneModifiers(unit.value());
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

        return Optional.of(new Ok<>(unit.withValue(formatContent(unit, wrapped))));
    }

    private static Node formatContent(PassUnit<Node> unit, Node wrapped) {
        return wrapped.withString(BLOCK_AFTER_CHILDREN, "\n" + "\t".repeat(Math.max(unit.state().depth() - 1, 0))).mapNodeList("children", children -> children.stream()
                .map(child -> child.withString(CONTENT_BEFORE_CHILD, "\n" + "\t".repeat(unit.state().depth())))
                .toList());
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> formatBlock(PassUnit<Node> unit) {
        if (!unit.value().is("block")) return Optional.empty();

        return Optional.of(new Ok<>(new InlinePassUnit<>(unit.state().exit(), formatContent(unit, unit.value()))));
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> formatRoot(PassUnit<Node> unit) {
        Node node = unit.value();
        if (!node.is("root")) return Optional.empty();

        final var node2 = ((Function<Node, Node>) node1 -> node1.mapNode("definition", definition -> definition.mapNodeList("children", Passer::indentRootChildren))).apply(node);
        return Optional.of(new Ok<>(unit.withValue(node2)));
    }

    private static Optional<Result<PassUnit<Node>, CompileError>> removeAccessModifiersFromDefinitions(PassUnit<Node> unit) {
        Node node = unit.value();
        if (!node.is("definition")) return Optional.empty();

        final var newNode = pruneModifiers(node)
                .mapNodeList("modifiers", Passer::replaceFinalWithConst)
                .mapNode("type", Passer::replaceVarWithAuto);

        return Optional.of(new Ok<>(unit.withValue(newNode)));
    }

    private static Result<PassUnit<Node>, CompileError> passNodeLists(PassUnit<Node> passedNodes) {
        return passNodeLists(passedNodes.state(), passedNodes.value());
    }

    private static Result<PassUnit<Node>, CompileError> passNodes(PassUnit<Node> passedBefore) {
        Node root = passedBefore.value();
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
                .foldLeftToResult(new InlinePassUnit<>(state, previous),
                        (current, tuple) -> passNodeList(current.state(), current.value(), tuple));
    }

    public static Result<PassUnit<Node>, CompileError> passNodeList(
            State state,
            Node root,
            Tuple<String, List<Node>> pair
    ) {
        final var propertyKey = pair.left();
        final var propertyValues = pair.right();
        return passNodeListInStream(state, propertyValues)
                .mapValue(list -> new InlinePassUnit<>(list.state(), root.withNodeList(propertyKey, list.value())));
    }

    private static Result<PassUnit<List<Node>>, CompileError> passNodeListInStream(State state, List<Node> elements) {
        return Streams.from(elements).foldLeftToResult(new InlinePassUnit<>(state, new ArrayList<>()), (current, currentElement) -> {
            final var currentState = current.state();
            final var currentElements = current.value();
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
            copy.add(result.value());
            return result.withValue(copy);
        });
    }

    private static List<Node> indentRootChildren(List<Node> rootChildren) {
        return rootChildren.stream()
                .map(child -> child.withString(CONTENT_AFTER_CHILD, "\n"))
                .toList();
    }

    private static Result<PassUnit<Node>, CompileError> foldNode(
            PassUnit<Node> current,
            Tuple<String, Node> tuple
    ) {
        final var currentState = current.state();
        final var currentRoot = current.value();
        final var pairKey = tuple.left();
        final var pairNode = tuple.right();

        return pass(currentState, pairNode).mapValue(passed -> new InlinePassUnit<>(passed.state(), currentRoot.withNode(pairKey, passed.value())));
    }
}