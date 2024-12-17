package magma;

import magma.api.Tuple;
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
import magma.app.compile.SymbolRule;
import magma.app.compile.TypeSplitter;
import magma.app.compile.rule.*;
import magma.app.error.ApplicationError;
import magma.app.error.Error;
import magma.app.error.FormattedError;
import magma.app.error.JavaError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

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

    private static Result<String, FormattedError> compile(String input) {
        return createJavaRootRule().parse(input)
                .flatMapValue(root -> pass(root, Main::modify))
                .flatMapValue(root -> pass(root, Main::format))
                .flatMapValue(node -> createCRootRule().generate(node));
    }

    private static Result<Node, FormattedError> format(Node node) {
        if (node.is("group")) {
            final var newChildren = node.findNodeList("children").orElseGet(MutableList::new)
                    .streamWithIndices()
                    .map(tuple -> {
                        final var index = tuple.left();
                        final var child = tuple.right();

                        if (index == 0) return child;
                        return child.withString("before-child", "\n");
                    })
                    .collect(MutableList.collector());

            return new Ok<>(node.withNodeList("children", newChildren));
        }

        return new Ok<>(node);
    }

    private static Result<Node, FormattedError> pass(Node root, Function<Node, Result<Node, FormattedError>> after) {
        return root.streamNodeLists()
                .foldLeft(new Ok<>(root), Main::fold)
                .flatMapValue(after);
    }

    private static Result<Node, FormattedError> modify(Node child) {
        final var children = child.findNodeList("children")
                .orElseGet(MutableList::new)
                .stream()
                .filter(node -> !node.is("package"))
                .map(node -> {
                    if (node.is("interface")) return node.retype("struct");
                    if (node.is("class") || node.is("record")) {
                        return node.retype("function");
                    }
                    return node;
                })
                .<List<Node>>foldLeft(new MutableList<>(), List::add);

        return new Ok<>(child.withNodeList("children", children));
    }

    private static Result<Node, FormattedError> fold(Result<Node, FormattedError> currentResult, Tuple<String, List<Node>> tuple) {
        return currentResult.flatMapValue(current -> {
            final var propertyKey = tuple.left();
            final var propertyValues = tuple.right();

            return propertyValues.stream()
                    .<Result<List<Node>, FormattedError>>foldLeft(new Ok<>(new MutableList<>()), (currentResult1, node) -> currentResult1.flatMapValue(current1 -> pass(node, Main::modify).mapValue(current1::add)))
                    .mapValue(newValues -> current.withNodeList(propertyKey, newValues));
        });
    }

    private static Rule createCRootRule() {
        final var children = new NodeListRule("children", new BracketSplitter(), new StripRule(createCRootMemberRule(), "before-child", ""));
        return new TypeRule("group", children);
    }

    private static Rule createJavaRootRule() {
        final var children = new NodeListRule("children", new BracketSplitter(), new StripRule(createJavaRootMemberRule()));
        return new TypeRule("group", children);
    }

    private static OrRule createCRootMemberRule() {
        return new OrRule(java.util.List.of(
                createImportRule(),
                new TypeRule("function", new ExactRule("def temp() => {}")),
                createStructRule()
        ));
    }

    private static TypeRule createStructRule() {
        return new TypeRule("struct", new PrefixRule("struct ", new SuffixRule(new StringRule("name"), " {\n\tvoid* __this__;\n}")));
    }

    private static OrRule createJavaRootMemberRule() {
        return new OrRule(java.util.List.of(
                new TypeRule("package", createNamespaceRule("package ")),
                createImportRule(),
                new TypeRule("record", new InfixRule(new DiscardRule(), "record ", new DiscardRule())),
                new TypeRule("class", new InfixRule(new DiscardRule(), "class ", new DiscardRule())),
                createInterfaceRule()
        ));
    }

    private static TypeRule createInterfaceRule() {
        final var name = new StripRule(new SymbolRule(new StringRule("name")));
        final var typeParams = new NodeListRule("type-params", new TypeSplitter(), new StringRule("value"));
        final var nameAndTypeParams = new OrRule(java.util.List.of(
                new InfixRule(name, "<", new StripRule(new SuffixRule(typeParams, ">"))),
                name
        ));

        final var maybeExtends = new OrRule(java.util.List.of(
                new InfixRule(nameAndTypeParams, "extends ", new StringRule("type")),
                nameAndTypeParams
        ));

        final var afterKeyword = new InfixRule(maybeExtends, "{", new DiscardRule());
        return new TypeRule("interface", new InfixRule(new DiscardRule(), "interface ", afterKeyword));
    }

    private static TypeRule createImportRule() {
        return new TypeRule("import", createNamespaceRule("import "));
    }

    private static Rule createNamespaceRule(String prefix) {
        final var namespace = new NodeListRule("namespace", new DelimiterSplitter("."), new StringRule("value"));
        return new PrefixRule(prefix, new SuffixRule(namespace, ";"));
    }
}
