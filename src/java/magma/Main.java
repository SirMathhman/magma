package magma;

import magma.api.JavaFiles;
import magma.api.Tuple;
import magma.compile.Node;
import magma.compile.error.ApplicationError;
import magma.compile.error.JavaError;
import magma.compile.lang.CLang;
import magma.compile.lang.JavaLang;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        final var source = Paths.get(".", "src", "java", "magma", "Main.java");
        JavaFiles.readString(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .match(input -> runWithInput(source, input), Optional::of)
                .ifPresent(error -> System.err.println(error.display()));
    }

    private static Optional<ApplicationError> runWithInput(Path source, String input) {
        return JavaLang.createJavaRootRule()
                .parse(input)
                .mapErr(ApplicationError::new)
                .mapValue(node -> pass(new State(), node, Tuple::new, Main::modify).right())
                .mapValue(node -> pass(new State(), node, Main::formatBefore, Main::formatAfter).right())
                .flatMapValue(parsed -> CLang.createCRootRule().generate(parsed).mapErr(ApplicationError::new))
                .mapValue(generated -> writeGenerated(generated, source.resolveSibling("Main.c"))).match(value -> value, Optional::of);
    }

    private static Tuple<State, Node> formatBefore(State state, Node node) {
        if (node.is("block")) {
            return new Tuple<>(state.enter(), node);
        }

        return new Tuple<>(state, node);
    }

    private static Tuple<State, Node> formatAfter(State state, Node node) {
        if (node.is("group")) {
            final var oldChildren = node.findNodeList("children");
            final var newChildren = new ArrayList<Node>();
            List<Node> orElse = oldChildren.orElse(Collections.emptyList());
            int i = 0;
            while (i < orElse.size()) {
                Node child = orElse.get(i);
                Node withString;
                if (state.depth() == 0 && i == 0) {
                    withString = child;
                } else {
                    final var indent = "\n" + "\t".repeat(state.depth());
                    withString = child.withString("before-child", indent);
                }
                newChildren.add(withString);
                i++;
            }

            return new Tuple<>(state, node
                    .withNodeList("children", newChildren)
                    .withString("after-children", "\n" + "\t".repeat(Math.max(state.depth() - 1, 0))));
        } else if (node.is("block")) {
            return new Tuple<>(state.exit(), node);
        } else {
            return new Tuple<>(state, node);
        }
    }

    private static Tuple<State, Node> pass(
            State state,
            Node node,
            BiFunction<State, Node, Tuple<State, Node>> beforePass,
            BiFunction<State, Node, Tuple<State, Node>> afterPass
    ) {
        final var withBefore = beforePass.apply(state, node);
        final var withNodeLists = withBefore.right()
                .streamNodeLists()
                .reduce(withBefore, (node1, tuple) -> passNodeLists(node1, tuple, beforePass, afterPass), (_, next) -> next);

        final var withNodes = withNodeLists.right()
                .streamNodes()
                .reduce(withNodeLists, (node1, tuple) -> passNode(node1, tuple, beforePass, afterPass), (_, next) -> next);

        return afterPass.apply(withNodes.left(), withNodes.right());
    }

    private static Tuple<State, Node> passNode(
            Tuple<State, Node> current,
            Tuple<String, Node> entry,
            BiFunction<State, Node, Tuple<State, Node>> beforePass,
            BiFunction<State, Node, Tuple<State, Node>> afterPass
    ) {
        final var oldState = current.left();
        final var oldNode = current.right();

        final var key = entry.left();
        final var value = entry.right();

        return pass(oldState, value, beforePass, afterPass).mapRight(right -> oldNode.withNode(key, right));
    }

    private static Tuple<State, Node> passNodeLists(
            Tuple<State, Node> current,
            Tuple<String, List<Node>> entry,
            BiFunction<State, Node, Tuple<State, Node>> beforePass,
            BiFunction<State, Node, Tuple<State, Node>> afterPass
    ) {
        final var oldState = current.left();
        final var oldChildren = current.right();

        final var key = entry.left();
        final var values = entry.right();

        var currentState = oldState;
        var currentChildren = new ArrayList<Node>();
        int i = 0;
        while (i < values.size()) {
            Node value = values.get(i);
            final var passed = pass(currentState, value, beforePass, afterPass);

            currentState = passed.left();
            currentChildren.add(passed.right());
            i++;
        }

        final var newNode = oldChildren.withNodeList(key, currentChildren);
        return new Tuple<>(oldState, newNode);
    }

    private static Tuple<State, Node> modify(State state, Node node) {
        Node result;
        if (node.is("group")) {
            final var oldChildren = node.findNodeList("children").orElse(new ArrayList<>());
            final var newChildren = oldChildren.stream()
                    .filter(oldChild -> !oldChild.is("package"))
                    .collect(Collectors.toCollection(ArrayList::new));

            result = node.withNodeList("children", newChildren);
        } else if (node.is("class")) {
            result = node.retype("struct");
        } else if (node.is("import")) {
            result = node.retype("include");
        } else if (node.is("method")) {
            result = node.retype("function");
        } else {
            result = node;
        }
        return new Tuple<>(state, result);
    }

    private static Optional<ApplicationError> writeGenerated(String generated, Path target) {
        return JavaFiles.writeString(target, generated)
                .map(JavaError::new)
                .map(ApplicationError::new);
    }
}
