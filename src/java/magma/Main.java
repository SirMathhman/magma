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
            var newChild = oldChild.is("class")
                    ? oldChild.retype("struct")
                    : oldChild;

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
        return new NodeListRule("children", new OrRule(List.of(
                createIncludesRule(),
                new TypeRule("struct", new PrefixRule("struct ", new SuffixRule(new StringRule("name"), " {}")))
        )));
    }

    private static NodeListRule createJavaRootRule() {
        return new NodeListRule("children", new OrRule(List.of(
                createNamespacedRule("package", "package "),
                createNamespacedRule("import", "import "),
                createClassRule()
        )));
    }

    private static TypeRule createClassRule() {
        final var name = new StripRule(new SymbolRule(new StringRule("name")));
        final var children = new NodeListRule("children", createClassMemberRule());
        return new TypeRule("class", new SplitRule(new DiscardRule(), new InfixSplitter("class ", new FirstLocator()), new SplitRule(name, new InfixSplitter("{", new FirstLocator()), new StripRule(new SuffixRule(children, "}")))));
    }

    private static ExactRule createClassMemberRule() {
        return new ExactRule("");
    }

    private static Rule createNamespacedRule(String type, String prefix) {
        final var namespace = new StringListRule("namespace", "\\.");
        return new TypeRule(type, new PrefixRule(prefix, new SuffixRule(namespace, ";")));
    }

    private static Rule createIncludesRule() {
        final var namespace = new StringListRule("namespace", "/");
        return new PrefixRule("#include <", new SuffixRule(namespace, ".h>\n"));
    }
}
