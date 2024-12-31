package magma.compile.pass;

import magma.api.Tuple;
import magma.compile.Node;
import magma.compile.State;

import java.util.List;
import java.util.stream.IntStream;

import static magma.compile.lang.CLang.FUNCTION_TYPE;
import static magma.compile.lang.CLang.INCLUDE_TYPE;
import static magma.compile.lang.CLang.STRUCT_TYPE;
import static magma.compile.lang.CommonLang.GROUP_AFTER_CHILDREN;
import static magma.compile.lang.CommonLang.GROUP_BEFORE_CHILD;
import static magma.compile.lang.CommonLang.GROUP_CHILDREN;
import static magma.compile.lang.CommonLang.GROUP_TYPE;
import static magma.compile.lang.JavaLang.CLASS_TYPE;
import static magma.compile.lang.JavaLang.IMPORT_TYPE;
import static magma.compile.lang.JavaLang.METHOD_TYPE;
import static magma.compile.lang.JavaLang.PACKAGE_TYPE;

public class Modifier implements Passer {
    private static Node passRootChild(Node child) {
        if (child.is("import")) return child.retype("include");
        if (child.is("class")) return child.retype("struct");
        return child;
    }

    private static Node attachIndent(State state, Tuple<Integer, Node> tuple) {
        final var index = tuple.left();
        final var node = tuple.right();
        final var depth = state.depth();
        if (index == 0 && depth == 0) return node;
        final var indent = createIndent(depth);
        return node.withString(GROUP_BEFORE_CHILD, indent);
    }

    private static String createIndent(int depth) {
        return "\n" + "\t".repeat(Math.max(depth, 0));
    }

    @Override
    public Tuple<State, Node> afterPass(State state, Node node) {
        if (node.is(GROUP_TYPE)) {
            final var exited = state.exit();
            final var depth = exited.depth();
            final var mapped = node
                    .map(GROUP_CHILDREN, children -> attachIndents(state, children))
                    .withString(GROUP_AFTER_CHILDREN, createIndent(depth - 1));

            return new Tuple<>(exited, mapped);
        }

        return new Tuple<>(state, node);
    }

    private static List<Node> attachIndents(State state, List<Node> children) {
        return IntStream.range(0, children.size())
                .mapToObj(index -> new Tuple<>(index, children.get(index)))
                .map(tuple -> attachIndent(state, tuple))
                .toList();
    }

    @Override
    public Tuple<State, Node> beforePass(State state, Node node) {
        if (node.is(GROUP_TYPE)) {
            return new Tuple<>(state.enter(), node.map(GROUP_CHILDREN, children -> children.stream()
                    .filter(child -> !child.is(PACKAGE_TYPE))
                    .map(Modifier::passRootChild)
                    .toList()));
        } else if (node.is(IMPORT_TYPE)) {
            return new Tuple<>(state, node.retype(INCLUDE_TYPE));
        } else if (node.is(CLASS_TYPE)) {
            return new Tuple<>(state, node.retype(STRUCT_TYPE));
        } else if (node.is(METHOD_TYPE)) {
            return new Tuple<>(state, node.retype(FUNCTION_TYPE));
        } else {
            return new Tuple<>(state, node);
        }
    }
}
