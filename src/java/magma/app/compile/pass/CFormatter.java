package magma.app.compile.pass;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.node.Input;
import magma.app.compile.node.MapNode;
import magma.app.compile.node.Node;
import magma.app.compile.node.NodeProperties;
import magma.app.compile.node.StringInput;
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
        NodeProperties<Input> inputNodeProperties = node.inputs();
        return inputNodeProperties.with("value", new StringInput("value")).orElse(new MapNode());
    }

    private static Node createRoot(List<Node> elements) {
        final var node = new MapNode("root");
        if (elements.isEmpty()) {
            return node;
        } else {
            NodeProperties<JavaList<Node>> javaListNodeProperties = node.nodeLists();
            return javaListNodeProperties.with(CONTENT_CHILDREN, new JavaList<Node>(elements)).orElse(new MapNode());
        }
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
        NodeProperties<Input> inputNodeProperties = block.inputs();
        Input propertyValue = new StringInput(CONTENT_AFTER_CHILDREN);
        Node node = inputNodeProperties.with(CONTENT_AFTER_CHILDREN, propertyValue).orElse(new MapNode());
        return node.nodeLists().map(CONTENT_CHILDREN, list -> new JavaList<>(((Function<List<Node>, List<Node>>) children -> attachIndent(state, children)).apply(list.unwrap()))).orElse(node);
    }

    private static List<Node> attachIndent(State state, List<Node> children) {
        return children.stream()
                .map(child -> {
                    formatIndent(state.depth() + 1);
                    NodeProperties<Input> inputNodeProperties = child.inputs();
                    return inputNodeProperties.with(CONTENT_BEFORE_CHILD, new StringInput(CONTENT_BEFORE_CHILD)).orElse(new MapNode());
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
                .map(child -> {
                    NodeProperties<Input> inputNodeProperties = child.inputs();
                    return inputNodeProperties.with(CONTENT_AFTER_CHILD, new StringInput(CONTENT_AFTER_CHILD)).orElse(new MapNode());
                })
                .toList();

        final var joined = String.join("_", namespace) + "_" + name;
        Node node4 = new MapNode("define");
        NodeProperties<Input> inputNodeProperties5 = node4.inputs();
        Input propertyValue8 = new StringInput(CONTENT_AFTER_CHILD);
        Node node5 = inputNodeProperties5.with(CONTENT_AFTER_CHILD, propertyValue8).orElse(new MapNode());
        Node node6 = new MapNode("if-not-defined");
        NodeProperties<Input> inputNodeProperties4 = node6.inputs();
        Input propertyValue7 = new StringInput(CONTENT_AFTER_CHILD);
        Node node7 = inputNodeProperties4.with(CONTENT_AFTER_CHILD, propertyValue7).orElse(new MapNode());
        NodeProperties<Input> inputNodeProperties2 = node5.inputs();
        Input propertyValue5 = new StringInput("value");
        NodeProperties<Input> inputNodeProperties3 = node7.inputs();
        Input propertyValue6 = new StringInput("value");
        final var headerElements = new ArrayList<>(List.of(
                inputNodeProperties3.with("value", propertyValue6).orElse(new MapNode()),
                inputNodeProperties2.with("value", propertyValue5).orElse(new MapNode())
        ));

        Node node3 = new MapNode("include");
        NodeProperties<Input> inputNodeProperties1 = node3.inputs();
        Input propertyValue4 = new StringInput(CONTENT_AFTER_CHILD);
        Node node = inputNodeProperties1.with(CONTENT_AFTER_CHILD, propertyValue4).orElse(new MapNode());
        List<Node> propertyValues = List.of(
                createSegment("."),
                createSegment(name)
        );
        NodeProperties<JavaList<Node>> javaListNodeProperties = node.nodeLists();
        JavaList<Node> propertyValue3 = new JavaList<>(propertyValues);
        final var sourceImport = javaListNodeProperties.with("namespace", propertyValue3).orElse(new MapNode());

        final var sourceElements = new ArrayList<>(List.of(sourceImport));
        newChildren.forEach(child -> {
            if (child.is("include")) {
                headerElements.add(child);
            } else {
                sourceElements.add(child);
            }
        });

        Node node2 = new MapNode("endif");
        NodeProperties<Input> inputNodeProperties = node2.inputs();
        Input propertyValue2 = new StringInput(CONTENT_AFTER_CHILD);
        headerElements.add(inputNodeProperties.with(CONTENT_AFTER_CHILD, propertyValue2).orElse(new MapNode()));

        Node node8 = new MapNode();
        Node propertyValue = createRoot(headerElements);
        NodeProperties<Node> nodeNodeProperties1 = node8.nodes();
        Node node9 = nodeNodeProperties1.with("header", propertyValue).orElse(new MapNode());
        Node propertyValue1 = createRoot(sourceElements);
        NodeProperties<Node> nodeNodeProperties = node9.nodes();
        return unit.withValue(nodeNodeProperties.with("source", propertyValue1).orElse(new MapNode()));
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