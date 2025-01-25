package magma.app.compile.pass;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Input;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.StringInput;
import magma.app.error.CompileError;
import magma.java.JavaList;
import magma.java.JavaOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static magma.app.lang.CommonLang.CONTENT_AFTER_CHILD;
import static magma.app.lang.CommonLang.CONTENT_AFTER_CHILDREN;
import static magma.app.lang.CommonLang.CONTENT_BEFORE_CHILD;
import static magma.app.lang.CommonLang.CONTENT_CHILDREN;
import static magma.app.compile.pass.Passer.by;

public class CFormatter implements Passer {
    static Node removeWhitespaceInContent(Node block) {
        return block.nodeLists().map(CONTENT_CHILDREN, list -> new JavaList<>(((Function<List<Node>, List<Node>>) CFormatter::removeWhitespaceInContentChildren).apply(list.unwrap()))).orElse(block);
    }

    private static List<Node> removeWhitespaceInContentChildren(List<Node> children) {
        return children.stream()
                .filter(child -> !child.is("whitespace"))
                .toList();
    }

    private static Node createSegment(String value) {
        Node node = new MapNode("segment");
        return node.inputs().with("value", new StringInput("value"));
    }

    private static Node createRoot(List<Node> elements) {
        final var node = new MapNode("root");
        return elements.isEmpty() ? node : node.nodeLists().with(CONTENT_CHILDREN, new JavaList<>(elements));
    }

    static boolean filterImport(Node child) {
        if (!child.is("import")) return true;

        final var namespace = JavaOptions.toNative(child.inputs().find("namespace").map(Input::unwrap)).orElse("");
        return !namespace.startsWith("java.util.function");
    }

    static PassUnit<Node> afterBlock(PassUnit<Node> inner) {
        return inner.exit().flattenNode(CFormatter::formatBlock);
    }

    private static Node formatBlock(State state, Node block) {
        if (JavaOptions.toNative(block.nodeLists().find(CONTENT_CHILDREN).map(JavaList::list)).orElse(new ArrayList<>()).isEmpty()) {
            return block.nodeLists().remove(CONTENT_CHILDREN).orElse(block);
        }

        formatIndent(state.depth());
        Node node = block.inputs().with(CONTENT_AFTER_CHILDREN, new StringInput(CONTENT_AFTER_CHILDREN));
        return node.nodeLists().map(CONTENT_CHILDREN, list -> new JavaList<>(((Function<List<Node>, List<Node>>) children -> attachIndent(state, children)).apply(list.unwrap()))).orElse(node);
    }

    private static List<Node> attachIndent(State state, List<Node> children) {
        return children.stream()
                .map(child -> {
                    formatIndent(state.depth() + 1);
                    return child.inputs().with(CONTENT_BEFORE_CHILD, new StringInput(CONTENT_BEFORE_CHILD));
                })
                .toList();
    }

    static String formatIndent(int state) {
        return "\n" + "\t".repeat(state);
    }

    static Node cleanupDefinition(Node definition) {
        Node node = definition.nodeLists().remove("annotations").orElse(definition);
        return node.nodeLists().remove("modifiers").orElse(node);
    }

    private static PassUnit<Node> cleanupNamespaced(PassUnit<Node> unit) {
        final var namespace = unit.findNamespace();
        final var name = unit.findName();

        Node node1 = unit.value();
        final var oldChildren = JavaOptions.toNative(node1.nodeLists().find(CONTENT_CHILDREN).map(JavaList::list)).orElse(Collections.emptyList());
        final var newChildren = oldChildren.stream()
                .filter(child -> !child.is("package"))
                .filter(CFormatter::filterImport)
                .map(child -> child.inputs().with(CONTENT_AFTER_CHILD, new StringInput(CONTENT_AFTER_CHILD)))
                .toList();

        final var joined = String.join("_", namespace) + "_" + name;
        Node node4 = new MapNode("define");
        Node node5 = node4.inputs().with(CONTENT_AFTER_CHILD, new StringInput(CONTENT_AFTER_CHILD));
        Node node6 = new MapNode("if-not-defined");
        Node node7 = node6.inputs().with(CONTENT_AFTER_CHILD, new StringInput(CONTENT_AFTER_CHILD));
        final var headerElements = new ArrayList<>(List.of(
                node7.inputs().with("value", new StringInput("value")),
                node5.inputs().with("value", new StringInput("value"))
        ));

        Node node3 = new MapNode("include");
        Node node = node3.inputs().with(CONTENT_AFTER_CHILD, new StringInput(CONTENT_AFTER_CHILD));
        List<Node> propertyValues = List.of(
                createSegment("."),
                createSegment(name)
        );
        final var sourceImport = node.nodeLists().with("namespace", new JavaList<>(propertyValues));

        final var sourceElements = new ArrayList<>(List.of(sourceImport));
        newChildren.forEach(child -> {
            if (child.is("include")) {
                headerElements.add(child);
            } else {
                sourceElements.add(child);
            }
        });

        Node node2 = new MapNode("endif");
        headerElements.add(node2.inputs().with(CONTENT_AFTER_CHILD, new StringInput(CONTENT_AFTER_CHILD)));

        Node node8 = new MapNode();
        Node propertyValue = createRoot(headerElements);
        Node node9 = node8.nodes().with("header", propertyValue);
        Node propertyValue1 = createRoot(sourceElements);
        return unit.withValue(node9.nodes().with("source", propertyValue1));
    }

    @Override
    public Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit.filter(by("block")).map(CFormatter::afterBlock)
                .or(() -> unit.filter(by("root")).map(CFormatter::cleanupNamespaced))
                .orElse(unit));
    }

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit.filter(by("block")).map(PassUnit::enter).map(inner -> inner.mapValue(CFormatter::removeWhitespaceInContent))
                .or(() -> unit.filterAndMapToValue(by("definition"), CFormatter::cleanupDefinition))
                .orElse(unit));
    }
}