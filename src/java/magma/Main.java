package magma;

import magma.compile.Node;
import magma.compile.error.ApplicationError;
import magma.compile.error.JavaError;
import magma.compile.rule.*;

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
        final var oldChildren = node.findNodeList("children").orElse(new ArrayList<>());
        final var newChildren = new ArrayList<Node>();
        for (Node oldChild : oldChildren) {
            if (oldChild.is("package")) continue;

            Node newChild;
            if (oldChild.is("class")) {
                newChild = oldChild.retype("struct");
            } else if (oldChild.is("import")) {
                newChild = oldChild.retype("include");
            } else newChild = oldChild;

            newChildren.add(newChild);
        }

        return node.withNodeList("children", newChildren);
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

    private static NodeListRule createCRootRule() {
        final var name = new StringRule("name");
        final var children = new NodeListRule("children", createStructMemberRule());
        return new NodeListRule("children", new OrRule(List.of(
                createIncludesRule(),
                new TypeRule("struct", new PrefixRule("struct ", new SplitRule(name, new InfixSplitter(" {", new FirstLocator()), new SuffixRule(children, "}")))),
                createWhitespaceRule()
        )));
    }

    private static Rule createStructMemberRule() {
        return new OrRule(List.of(

        ));
    }

    private static NodeListRule createJavaRootRule() {
        return new NodeListRule("children", new OrRule(List.of(
                createNamespacedRule("package", "package "),
                createNamespacedRule("import", "import "),
                createClassRule(),
                createWhitespaceRule()
        )));
    }

    private static TypeRule createClassRule() {
        final var name = new StripRule(new SymbolRule(new StringRule("name")));
        final var children = new NodeListRule("children", createClassMemberRule());
        return new TypeRule("class", new SplitRule(new DiscardRule(), new InfixSplitter("class ", new FirstLocator()), new SplitRule(name, new InfixSplitter("{", new FirstLocator()), new StripRule(new SuffixRule(children, "}")))));
    }

    private static Rule createClassMemberRule() {
        return new OrRule(List.of(
                new TypeRule("method", new SplitRule(new DiscardRule(), new InfixSplitter("(", new FirstLocator()), new DiscardRule())),
                createWhitespaceRule()
        ));
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
        return new TypeRule("include", new PrefixRule("#include <", new SuffixRule(namespace, ".h>\n")));
    }
}
