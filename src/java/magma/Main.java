package magma;

import magma.api.Tuple;
import magma.compile.Node;
import magma.compile.error.ApplicationError;
import magma.compile.error.JavaError;
import magma.compile.rule.DiscardRule;
import magma.compile.rule.ExactRule;
import magma.compile.rule.LazyRule;
import magma.compile.rule.LocatingSplitter;
import magma.compile.rule.NodeListRule;
import magma.compile.rule.OrRule;
import magma.compile.rule.PrefixRule;
import magma.compile.rule.Rule;
import magma.compile.rule.SplitRule;
import magma.compile.rule.StringListRule;
import magma.compile.rule.StringRule;
import magma.compile.rule.StripRule;
import magma.compile.rule.SuffixRule;
import magma.compile.rule.SymbolRule;
import magma.compile.rule.TypeRule;
import magma.compile.rule.locate.BackwardsLocator;
import magma.compile.rule.locate.FirstLocator;
import magma.compile.rule.locate.LastLocator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class Main {
    public static void main(String[] args) throws IOException {
        final var source = Paths.get(".", "src", "java", "magma", "Main.java");
        final var input = Files.readString(source);

        createJavaRootRule()
                .parse(input)
                .mapErr(ApplicationError::new)
                .mapValue(node -> pass(new State(), node, Tuple::new, Main::modify).right())
                .mapValue(node -> pass(new State(), node, Main::formatBefore, Main::formatAfter).right())
                .flatMapValue(parsed -> createCRootRule().generate(parsed).mapErr(ApplicationError::new))
                .mapValue(generated -> writeGenerated(source, generated)).match(value -> value, Optional::of)
                .ifPresent(error -> System.err.println(error.display()));
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
            for (int i = 0; i < orElse.size(); i++) {
                Node child = orElse.get(i);
                Node withString;
                if (state.depth() == 0 && i == 0) {
                    withString = child;
                } else {
                    final var indent = "\n" + "\t".repeat(state.depth());
                    withString = child.withString("before-child", indent);
                }
                newChildren.add(withString);
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
        for (Node value : values) {
            final var passed = pass(currentState, value, beforePass, afterPass);

            currentState = passed.left();
            currentChildren.add(passed.right());
        }

        final var newNode = oldChildren.withNodeList(key, currentChildren);
        return new Tuple<>(oldState, newNode);
    }

    private static Tuple<State, Node> modify(State state, Node node) {
        Node result;
        if (node.is("group")) {
            final var oldChildren = node.findNodeList("children").orElse(new ArrayList<>());
            final var newChildren = new ArrayList<Node>();
            for (Node oldChild : oldChildren) {
                if (oldChild.is("package")) continue;
                newChildren.add(oldChild);
            }

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

    private static Optional<ApplicationError> writeGenerated(Path source, String generated) {
        try {
            final var target = source.resolveSibling("Main.c");
            Files.writeString(target, generated);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(new ApplicationError(new JavaError(e)));
        }
    }

    private static Rule createCRootRule() {
        return createGroupRule(createCRootMemberRule());
    }

    private static OrRule createCRootMemberRule() {
        return new OrRule(List.of(
                createIncludesRule(),
                createStructRule(),
                createWhitespaceRule()
        ));
    }

    private static Rule createStructRule() {
        final var name = new StringRule("name");
        final var wrapped = wrapInBlock(name, createStructMemberRule());
        return new TypeRule("struct", new PrefixRule("struct ", wrapped));
    }

    private static Rule createGroupRule(Rule childRule) {
        final var children = new NodeListRule("children", new StripRule("before-child", childRule, "after-child"));
        return new TypeRule("group", new StripRule("before-children", children, "after-children"));
    }

    private static Rule createStructMemberRule() {
        return new OrRule(List.of(
                createFunctionRule(),
                createWhitespaceRule()
        ));
    }

    private static TypeRule createFunctionRule() {
        final var type = new NodeRule("type", createTypeRule());
        return new TypeRule("function", new SplitRule(type, new LocatingSplitter(" ", new FirstLocator()), new SuffixRule(new StringRule("name"), "(){}")));
    }

    private static Rule createJavaRootRule() {
        return new TypeRule("group", new NodeListRule("children", new OrRule(List.of(
                createNamespacedRule("package", "package "),
                createNamespacedRule("import", "import "),
                createClassRule(),
                createWhitespaceRule()
        ))));
    }

    private static TypeRule createClassRule() {
        final var name = new StripRule(new SymbolRule(new StringRule("name")));
        final var rightRule = wrapInBlock(name, createClassMemberRule());
        return new TypeRule("class", new SplitRule(new DiscardRule(), new LocatingSplitter("class ", new FirstLocator()), rightRule));
    }

    private static SplitRule wrapInBlock(Rule beforeBlock, Rule blockMember) {
        final var value = new NodeRule("value", createGroupRule(blockMember));
        final var blockRule = new TypeRule("block", value);
        return new SplitRule(beforeBlock, new LocatingSplitter(" {", new FirstLocator()), new StripRule(new SuffixRule(new NodeRule("value", blockRule), "}")));
    }

    private static Rule createClassMemberRule() {
        return new OrRule(List.of(
                createMethodRule(),
                createWhitespaceRule()
        ));
    }

    private static TypeRule createMethodRule() {
        final var type = new NodeRule("type", createTypeRule());
        final var leftRule = new SplitRule(new DiscardRule(), new LocatingSplitter(" ", new BackwardsLocator()), type);
        final var beforeParams = new SplitRule(leftRule, new LocatingSplitter(" ", new LastLocator()), new StringRule("name"));
        return new TypeRule("method", new SplitRule(beforeParams, new LocatingSplitter("(", new FirstLocator()), new DiscardRule()));
    }

    private static Rule createTypeRule() {
        final LazyRule type = new LazyRule();
        type.set(new OrRule(List.of(
                createGenericRule(type),
                createSymbolRule()
        )));
        return type;
    }

    private static TypeRule createSymbolRule() {
        return new TypeRule("symbol", new SymbolRule(new StringRule("value")));
    }

    private static TypeRule createGenericRule(LazyRule type) {
        final var parent = new StringRule("parent");
        return new TypeRule("generic", new SplitRule(parent, new LocatingSplitter("<", new FirstLocator()), new SuffixRule(type, ">")));
    }

    private static TypeRule createWhitespaceRule() {
        return new TypeRule("whitespace", new StripRule(new ExactRule("")));
    }

    private static Rule createNamespacedRule(String type, String prefix) {
        final var namespace = new StringListRule("namespace", "\\.");
        return new TypeRule(type, new PrefixRule(prefix, new SuffixRule(namespace, ";")));
    }

    private static Rule createIncludesRule() {
        final var namespace = new StringListRule("namespace", "/");
        return new TypeRule("include", new PrefixRule("#include \"", new SuffixRule(namespace, ".h\"")));
    }
}
