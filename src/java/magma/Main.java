package magma;

import magma.api.Tuple;
import magma.compile.Node;
import magma.compile.error.ApplicationError;
import magma.compile.error.JavaError;
import magma.compile.rule.DiscardRule;
import magma.compile.rule.ExactRule;
import magma.compile.rule.FirstLocator;
import magma.compile.rule.InfixSplitter;
import magma.compile.rule.LastLocator;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) throws IOException {
        final var source = Paths.get(".", "src", "java", "magma", "Main.java");
        final var input = Files.readString(source);

        createJavaRootRule()
                .parse(input)
                .mapErr(ApplicationError::new)
                .mapValue(node -> pass(node, Main::modify))
                .mapValue(node -> pass(node, Main::format))
                .flatMapValue(parsed -> createCRootRule().generate(parsed).mapErr(ApplicationError::new))
                .mapValue(generated -> writeGenerated(source, generated)).match(value -> value, Optional::of)
                .ifPresent(error -> System.err.println(error.display()));
    }

    private static Node format(Node node) {
        if (!node.is("group")) return node;

        final var oldChildren = node.findNodeList("children");
        final var newChildren = new ArrayList<Node>();
        List<Node> orElse = oldChildren.orElse(Collections.emptyList());
        for (int i = 0; i < orElse.size(); i++) {
            Node child = orElse.get(i);
            Node withString = i == 0 ? child : child.withString("before-child", "\n");
            newChildren.add(withString);
        }

        return node.withNodeList("children", newChildren);
    }

    private static Node pass(Node node, Function<Node, Node> afterPass) {
        final var withNodeLists = node.streamNodeLists().reduce(node, (node1, tuple) -> passNodeLists(node1, tuple, afterPass), (_, next) -> next);
        final var withNodes = withNodeLists.streamNodes().reduce(withNodeLists, (node1, tuple) -> passNode(node1, tuple, afterPass), (_, next) -> next);
        return afterPass.apply(withNodes);
    }

    private static Node passNode(Node node, Tuple<String, Node> tuple, Function<Node, Node> afterPass) {
        final var key = tuple.left();
        final var value = tuple.right();
        final var passed = pass(value, afterPass);
        return node.withNode(key, passed);
    }

    private static Node passNodeLists(Node node1, Tuple<String, List<Node>> tuple, Function<Node, Node> afterPass) {
        final var key = tuple.left();
        final var values = tuple.right();
        var newChildren = new ArrayList<Node>();
        for (Node value : values) {
            final var passed = pass(value, afterPass);
            newChildren.add(passed);
        }
        return node1.withNodeList(key, newChildren);
    }

    private static Node modify(Node node) {
        if (node.is("group")) {
            final var oldChildren = node.findNodeList("children").orElse(new ArrayList<>());
            final var newChildren = new ArrayList<Node>();
            for (Node oldChild : oldChildren) {
                if (oldChild.is("package")) continue;
                newChildren.add(oldChild);
            }

            return node.withNodeList("children", newChildren);
        } else if (node.is("class")) {
            return node.retype("struct");
        } else if (node.is("import")) {
            return node.retype("include");
        } else if (node.is("method")) {
            return node.retype("function");
        } else {
            return node;
        }
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
        final var value = new NodeRule("value", createGroupRule(createStructMemberRule()));
        return new TypeRule("struct", new PrefixRule("struct ", new SplitRule(name, new InfixSplitter(" {", new FirstLocator()), new SuffixRule(value, "}"))));
    }

    private static Rule createGroupRule(Rule childRule) {
        final var children = new NodeListRule("children", new StripRule("before-child", childRule, "after-child"));
        return new TypeRule("group", new StripRule("before-children", children, ""));
    }

    private static Rule createStructMemberRule() {
        return new OrRule(List.of(
                new TypeRule("function", new PrefixRule("void ", new SuffixRule(new StringRule("name"), "(){}"))),
                createWhitespaceRule()
        ));
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
        final var value = new NodeRule("value", createGroupRule(createClassMemberRule()));
        return new TypeRule("class", new SplitRule(new DiscardRule(), new InfixSplitter("class ", new FirstLocator()), new SplitRule(name, new InfixSplitter("{", new FirstLocator()), new StripRule(new SuffixRule(value, "}")))));
    }

    private static Rule createClassMemberRule() {
        return new OrRule(List.of(
                createMethodRule(),
                createWhitespaceRule()
        ));
    }

    private static TypeRule createMethodRule() {
        final var beforeParams = new SplitRule(new DiscardRule(), new InfixSplitter(" ", new LastLocator()), new StringRule("name"));
        return new TypeRule("method", new SplitRule(beforeParams, new InfixSplitter("(", new FirstLocator()), new DiscardRule()));
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
