package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws IOException {
        final var source = Paths.get(".", "src", "java", "magma", "Main.java");
        final var input = Files.readString(source);

        createJavaRootRule()
                .parse(input)
                .mapErr(ApplicationError::new)
                .flatMapValue(parsed -> createCRootRule().generate(parsed).mapErr(ApplicationError::new))
                .mapValue(generated -> writeGenerated(source, generated)).match(value -> value, Optional::of)
                .ifPresent(error -> System.err.println(error.display()));
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
        return new NodeListRule("children", createCRootSegmentRule());
    }

    private static NodeListRule createJavaRootRule() {
        return new NodeListRule("children", createJavaRootSegmentRule());
    }

    private static OrRule createCRootSegmentRule() {
        return new OrRule(List.of(
                createIncludesRule(),
                new TypeRule("struct", new ExactRule("struct Temp {}"))
        ));
    }

    private static OrRule createJavaRootSegmentRule() {
        return new OrRule(List.of(
                createNamespacedRule("package", "package "),
                createNamespacedRule("import", "import ")
        ));
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
