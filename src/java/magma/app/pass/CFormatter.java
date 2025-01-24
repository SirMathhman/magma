package magma.app.pass;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.lang.CommonLang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static magma.app.lang.CommonLang.CONTENT_AFTER_CHILD;
import static magma.app.lang.CommonLang.CONTENT_CHILDREN;
import static magma.app.pass.Passer.by;

public class CFormatter implements Passer {
    static Node removeWhitespace(Node block) {
        return block.mapNodeList(CommonLang.CONTENT_CHILDREN, children -> {
            return children.stream()
                    .filter(child -> !child.is("whitespace"))
                    .toList();
        });
    }

    static Node cleanupNamespaced(Node root) {
        final var oldChildren = root.findNodeList(CommonLang.CONTENT_CHILDREN).orElse(Collections.emptyList());
        final var newChildren = oldChildren.stream()
                .filter(child -> !child.is("package"))
                .filter(CFormatter::filterImport)
                .map(child -> child.withString(CONTENT_AFTER_CHILD, "\n"))
                .toList();

        final var headerElements = new ArrayList<Node>();
        final var sourceElements = new ArrayList<Node>();
        newChildren.forEach(child -> {
            if (child.is("include")) {
                headerElements.add(child);
            } else {
                sourceElements.add(child);
            }
        });

        return new MapNode()
                .withNode("header", createRoot(headerElements))
                .withNode("source", createRoot(sourceElements));
    }

    private static Node createRoot(List<Node> elements) {
        final var node = new MapNode("root");
        return elements.isEmpty()
                ? node
                : node.withNodeList(CONTENT_CHILDREN, elements);
    }

    static boolean filterImport(Node child) {
        if (!child.is("import")) return true;

        final var namespace = child.findString("namespace").orElse("");
        return !namespace.startsWith("java.util.function");
    }

    static PassUnit<Node> formatBlock(PassUnit<Node> inner) {
        return inner.exit().flattenNode((State state, Node block) -> {
            if (block.findNodeList(CommonLang.CONTENT_CHILDREN).orElse(new ArrayList<Node>()).isEmpty()) {
                return block.removeNodeList(CommonLang.CONTENT_CHILDREN);
            }

            return block.withString(CommonLang.CONTENT_AFTER_CHILDREN, formatIndent(state.depth()))
                    .mapNodeList(CommonLang.CONTENT_CHILDREN, children -> attachIndent(state, children));
        });
    }

    private static List<Node> attachIndent(State state, List<Node> children) {
        return children.stream()
                .map(child -> child.withString(CommonLang.CONTENT_BEFORE_CHILD, formatIndent(state.depth() + 1)))
                .toList();
    }

    static String formatIndent(int state) {
        return "\n" + "\t".repeat(state);
    }

    static Node cleanupDefinition(Node definition) {
        return definition.removeNodeList("annotations").removeNodeList("modifiers");
    }

    @Override
    public Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit.filter(by("block")).map(CFormatter::formatBlock)
                .or(() -> unit.filterAndMapToValue(by("root"), CFormatter::cleanupNamespaced))
                .orElse(unit));
    }

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit.filter(by("block")).map(PassUnit::enter).map(inner -> inner.mapValue(CFormatter::removeWhitespace))
                .or(() -> unit.filterAndMapToValue(by("definition"), CFormatter::cleanupDefinition))
                .orElse(unit));
    }
}