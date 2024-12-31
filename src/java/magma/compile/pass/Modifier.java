package magma.compile.pass;

import magma.api.Tuple;
import magma.compile.Node;
import magma.compile.State;

import java.util.stream.IntStream;

import static magma.compile.lang.CLang.FUNCTION_TYPE;
import static magma.compile.lang.CLang.INCLUDE_TYPE;
import static magma.compile.lang.CLang.STRUCT_TYPE;
import static magma.compile.lang.CommonLang.GROUP_BEFORE_CHILD;
import static magma.compile.lang.CommonLang.GROUP_CHILDREN;
import static magma.compile.lang.CommonLang.GROUP_TYPE;
import static magma.compile.lang.JavaLang.CLASS_TYPE;
import static magma.compile.lang.JavaLang.IMPORT_TYPE;
import static magma.compile.lang.JavaLang.METHOD_TYPE;
import static magma.compile.lang.JavaLang.PACKAGE_TYPE;
import static magma.compile.lang.JavaLang.ROOT_CHILDREN;
import static magma.compile.lang.JavaLang.ROOT_TYPE;

public class Modifier implements Passer {
    private static Node passRootChild(Node child) {
        if (child.is("import")) return child.retype("include");
        if (child.is("class")) return child.retype("struct");
        return child;
    }

    private static Node after(Node node) {
        if (node.is(ROOT_TYPE)) {
            return node.map(ROOT_CHILDREN, children -> children.stream()
                    .filter(child -> !child.is(PACKAGE_TYPE))
                    .map(Modifier::passRootChild)
                    .toList());
        }
        if (node.is(IMPORT_TYPE)) return node.retype(INCLUDE_TYPE);
        if (node.is(CLASS_TYPE)) return node.retype(STRUCT_TYPE);
        if (node.is(METHOD_TYPE)) return node.retype(FUNCTION_TYPE);
        return node;
    }

    private static Node getNode(Tuple<Integer, Node> tuple) {
        final var index = tuple.left();
        final var node = tuple.right();
        if (index == 0) return node;
        return node.withString(GROUP_BEFORE_CHILD, "\n");
    }

    @Override
    public Tuple<State, Node> afterPass(State state, Node node) {
        if (node.is(GROUP_TYPE)) {
            final var map = node.map(GROUP_CHILDREN, children -> {
                return IntStream.range(0, children.size())
                        .mapToObj(index -> new Tuple<>(index, children.get(index)))
                        .map(Modifier::getNode)
                        .toList();
            });
            return new Tuple<>(state, map);
        }

        return new Tuple<>(state, node);
    }

    @Override
    public Tuple<State, Node> beforePass(State state, Node node) {
        return new Tuple<>(state, after(node));
    }
}
