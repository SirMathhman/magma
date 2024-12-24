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
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws IOException {
        final var source = Paths.get(".", "src", "java", "magma", "Main.java");
        final var input = Files.readString(source);

        createJavaRootRule()
                .parse(input)
                .mapErr(ApplicationError::new)
                .mapValue(Main::pass)
                .flatMapValue(parsed -> createCRootRule().generate(parsed).mapErr(ApplicationError::new))
                .mapValue(generated -> writeGenerated(source, generated)).match(value -> value, Optional::of)
                .ifPresent(error -> System.err.println(error.display()));
    }

    private static Node pass(Node node) {
        final var mapped = passNodeLists(node);
        return afterPass(mapped);
    }

    private static Node passNodeLists(Node node) {
        return node.streamNodeLists().reduce(node, Main::passNodeLists, (_, next) -> next);
    }

    private static Node passNodeLists(Node node1, Tuple<String, List<Node>> tuple) {
        final var key = tuple.left();
        final var values = tuple.right();
        var newChildren = new ArrayList<Node>();
        for (Node value : values) {
            final var passed = pass(value);
            newChildren.add(passed);
        }
        return node1.withNodeList(key, newChildren);
    }

    private static Node afterPass(Node node) {
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
        final var name = new StringRule("name");
        final var children = new NodeListRule("children", createStructMemberRule());
        return new TypeRule("group", new NodeListRule("children", new OrRule(List.of(
                createIncludesRule(),
                new TypeRule("struct", new PrefixRule("struct ", new SplitRule(name, new InfixSplitter(" {", new FirstLocator()), new SuffixRule(children, "}")))),
                createWhitespaceRule()
        ))));
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
        final var children = new NodeListRule("children", createClassMemberRule());
        return new TypeRule("class", new SplitRule(new DiscardRule(), new InfixSplitter("class ", new FirstLocator()), new SplitRule(name, new InfixSplitter("{", new FirstLocator()), new StripRule(new SuffixRule(children, "}")))));
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
        return new TypeRule("include", new PrefixRule("#include \"", new SuffixRule(namespace, ".h\"\n")));
    }
}
