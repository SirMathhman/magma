package magma;

import magma.api.collect.List;
import magma.api.collect.MutableList;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Options;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.rule.*;
import magma.app.error.ApplicationError;
import magma.app.error.Error;
import magma.app.error.FormattedError;
import magma.app.error.JavaError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        run().map(Error::display).ifPresent(System.err::println);
    }

    private static Option<ApplicationError> run() {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            return stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .map(Main::runWithSource)
                    .flatMap(Options::asStream)
                    .findFirst()
                    .<Option<ApplicationError>>map(Some::new)
                    .orElseGet(None::new);
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError(e)));
        }
    }

    private static Option<ApplicationError> runWithSource(Path source) {
        final var relativeSourceParent = Main.SOURCE_DIRECTORY.relativize(source.getParent());
        final var targetParent = TARGET_DIRECTORY.resolve(relativeSourceParent);
        if (!Files.exists(targetParent)) {
            return createDirectoriesSafe(targetParent).or(() -> compileAndRead(source, targetParent));
        }

        return compileAndRead(source, targetParent);
    }

    private static Option<ApplicationError> compileAndRead(Path source, Path targetParent) {
        final var name = source.getFileName().toString();
        final var separator = name.indexOf('.');
        final var nameWithoutExt = name.substring(0, separator);
        final var target = targetParent.resolve(nameWithoutExt + ".mgs");
        return readSafe(source)
                .mapValue(input -> compileAndWrite(input, target))
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> compileAndWrite(String input, Path target) {
        return compile(input)
                .mapErr(ApplicationError::new)
                .mapValue(output -> writeSafe(target, output))
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> writeSafe(Path target, String output) {
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

    private static Option<ApplicationError> createDirectoriesSafe(Path targetParent) {
        try {
            Files.createDirectories(targetParent);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError(e)));
        }
    }

    private static Result<String, FormattedError> compile(String root) {
        return createJavaRootRule().parse(root)
                .mapValue(Main::pass)
                .flatMapValue(node -> createMagmaRootRule().generate(node));
    }

    private static Node pass(Node root) {
        final var children = root.findNodeList("children")
                .orElseGet(MutableList::new)
                .stream()
                .filter(node -> !node.is("package"))
                .map(node -> {
                    if (node.is("interface")) return node.retype("trait");
                    if (node.is("class") || node.is("record")) return node.retype("function");
                    return node;
                })
                .<List<Node>>foldLeft(new MutableList<>(), List::add);

        return root.withNodeList("children", children);
    }

    private static SplitRule createMagmaRootRule() {
        return new SplitRule("children", createMagmaRootMemberRule());
    }

    private static SplitRule createJavaRootRule() {
        return new SplitRule("children", new StripRule(createJavaRootMemberRule()));
    }

    private static OrRule createMagmaRootMemberRule() {
        return new OrRule(java.util.List.of(
                new TypeRule("import", new ExactRule("import temp;")),
                new TypeRule("function", new ExactRule("def temp() => {}")),
                new TypeRule("trait", new ExactRule("trait Temp {}"))
        ));
    }

    private static OrRule createJavaRootMemberRule() {
        return new OrRule(java.util.List.of(
                new TypeRule("package", new PrefixRule("package ", new DiscardRule())),
                new TypeRule("import", new PrefixRule("import ", new DiscardRule())),
                new TypeRule("record", new InfixRule(new DiscardRule(), "record ", new DiscardRule())),
                new TypeRule("class", new InfixRule(new DiscardRule(), "class ", new DiscardRule())),
                new TypeRule("interface", new InfixRule(new DiscardRule(), "interface ", new DiscardRule()))
        ));
    }
}
