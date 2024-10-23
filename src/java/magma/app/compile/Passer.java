package magma.app.compile;

import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static magma.app.compile.lang.CommonLang.CHILDREN;
import static magma.app.compile.lang.JavaLang.*;
import static magma.app.compile.lang.MagmaLang.FUNCTION;

public class Passer {
    static Node pass(Node node) {
        return node.mapNodeList(CHILDREN, Passer::passChildren).orElse(node);
    }

    private static List<Node> passChildren(List<Node> children) {
        return children.stream()
                .filter(child -> !child.is(JavaLang.PACKAGE))
                .map(Passer::passRootChild)
                .toList();
    }

    private static Node passRootChild(Node node) {
        return passRecord(node)
                .or(() -> passInterface(node))
                .or(() -> passClass(node))
                .orElse(node);
    }

    private static Optional<Node> passClass(Node node) {
        if (node.is(CLASS)) {
            return Optional.of(node.retype(FUNCTION));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Node> passRecord(Node node) {
        return node.is(RECORD) ? Optional.of(node.retype(FUNCTION)) : Optional.empty();
    }

    private static Optional<Node> passInterface(Node node) {
        if (!node.is(INTERFACE)) return Optional.empty();

        final var retype = node.retype(MagmaLang.TRAIT);

        final var withModifiers = retype.mapStringList(MODIFIERS, modifiers -> {
            var newList = new ArrayList<String>();
            if (modifiers.contains("public")) newList.add("export");
            return newList;
        }).orElse(retype);

        final var withChildren = withModifiers.mapNodeList(CHILDREN, children -> children.stream()
                .map(Passer::passClassMember)
                .toList()).orElse(withModifiers);

        return Optional.of(withChildren);
    }

    private static Node passClassMember(Node child) {
        if (child.is(METHOD)) return child.retype(FUNCTION);
        return child;
    }
}
