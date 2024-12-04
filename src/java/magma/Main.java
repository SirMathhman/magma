package magma;

import magma.compile.Node;
import magma.api.error.Error;
import magma.java.JavaError;
import magma.java.JavaList;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.compile.rule.*;
import magma.api.stream.Streams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");

    public static void main(String[] args) {
        findSources()
                .mapValue(Main::runWithSources)
                .match(onOk -> onOk, Some::new)
                .ifPresent(e -> System.err.println(e.display()));
    }

    private static Result<List<Path>, Error> findSources() {
        try (Stream<Path> stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());

            return new Ok<>(sources);
        } catch (IOException e) {
            return new Err<>(new JavaError(e));
        }
    }

    private static Option<Error> runWithSources(List<Path> sources) {
        return Streams.from(sources)
                .map(Main::runWithSource)
                .flatMap(Streams::fromOption)
                .next();
    }

    private static Option<Error> runWithSource(Path source) {
        final var fileName = source.getFileName().toString();
        final var separator = fileName.indexOf('.');
        final var name = fileName.substring(0, separator);

        final var relativized = SOURCE_DIRECTORY.relativize(source.getParent());
        final var targetParent = Paths.get(".", "src", "magma").resolve(relativized);
        return createDirectories(targetParent).or(() -> {
            final var target = targetParent.resolve(name + ".mgs");

            return readSafe(source)
                    .flatMapValue(Main::compile)
                    .mapValue(output -> writeSafe(target, output))
                    .match(onOk -> onOk, Some::new);
        });
    }

    private static Option<Error> createDirectories(Path targetParent) {
        if (Files.exists(targetParent)) return new None<>();

        try {
            Files.createDirectories(targetParent);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new JavaError(e));
        }
    }

    private static Result<String, ApplicationError> compile(String input) {
        final var javaRule = createJavaRootRule();
        final var magmaRule = createMagmaRootRule();
        return javaRule.parse(input)
                .mapValue(Main::pass)
                .flatMapValue(magmaRule::generate);
    }

    private static Node pass(Node root) {
        return root.mapNodeList("children", Main::passRootChildren).orElse(root);
    }

    private static JavaList<Node> passRootChildren(JavaList<Node> children) {
        return children.stream()
                .filter(child -> !child.is("package"))
                .map(Main::passRootMember)
                .foldLeft(new JavaList<Node>(), JavaList::add);
    }

    private static Node passRootMember(Node rootMember) {
        if (rootMember.is("record") || rootMember.is("class")) {
            return rootMember.retype("function");
        } else if (rootMember.is("interface")) {
            return rootMember.retype("trait");
        } else {
            return rootMember;
        }
    }

    private static SplitRule createJavaRootRule() {
        return new SplitRule("children", createJavaRootMemberRule());
    }

    private static SplitRule createMagmaRootRule() {
        return new SplitRule("children", createMagmaRootMemberRule());
    }

    private static Option<Error> writeSafe(Path target, String output) {
        try {
            Files.writeString(target, output);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError(e)));
        }
    }

    private static Result<String, ApplicationError> readSafe(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(new ApplicationError(new JavaError(e)));
        }
    }

    private static OrRule createMagmaRootMemberRule() {
        return new OrRule(List.of(
                createImportRule(),
                createFunctionRule(),
                createTraitRule()
        ));
    }

    private static OrRule createJavaRootMemberRule() {
        return new OrRule(List.of(
                createNamespaceRule("package", "package "),
                createImportRule(),
                new TypeRule("record", new InfixRule("record")),
                new TypeRule("class", new InfixRule("class")),
                new TypeRule("interface", new InfixRule("interface"))
        ));
    }

    private static Rule createFunctionRule() {
        return new TypeRule("function", new ExactRule("class def Temp() => {}"));
    }

    private static Rule createTraitRule() {
        return new TypeRule("trait", new ExactRule("trait Temp {}"));
    }

    private static Rule createImportRule() {
        return createNamespaceRule("import", "import ");
    }

    private static Rule createNamespaceRule(String type, String prefix) {
        return new TypeRule(type, new StripRule(new PrefixRule(prefix, new SuffixRule(new StringRule(Node.NAMESPACE_VALUE), ";"))));
    }
}
