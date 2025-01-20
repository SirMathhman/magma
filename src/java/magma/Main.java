package magma;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.ApplicationError;
import magma.app.error.CompileError;
import magma.app.error.JavaError;
import magma.app.error.context.StringContext;
import magma.app.filter.NumberFilter;
import magma.app.filter.SymbolFilter;
import magma.app.locate.FirstLocator;
import magma.app.locate.LastLocator;
import magma.app.locate.LocateTypeSeparator;
import magma.app.locate.ParenthesesMatcher;
import magma.app.rule.DivideRule;
import magma.app.rule.ExactRule;
import magma.app.rule.FilterRule;
import magma.app.rule.InfixRule;
import magma.app.rule.LazyRule;
import magma.app.rule.NodeRule;
import magma.app.rule.OrRule;
import magma.app.rule.PrefixRule;
import magma.app.rule.Rule;
import magma.app.rule.StringRule;
import magma.app.rule.StripRule;
import magma.app.rule.SuffixRule;
import magma.app.rule.TypeRule;
import magma.java.JavaFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");
    public static final String DEFAULT_VALUE = "value";

    public static void main(String[] args) {
        collect().mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(Main::runWithSources)
                .match(Function.identity(), Optional::of)
                .ifPresent(error -> System.err.println(error.display()));
    }

    private static Result<Set<Path>, IOException> collect() {
        return JavaFiles.walkWrapped(SOURCE_DIRECTORY).mapValue(paths -> paths.stream()
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .collect(Collectors.toSet()));
    }

    private static Optional<ApplicationError> runWithSources(Set<Path> sources) {
        return sources.stream()
                .map(Main::runWithSource)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Optional<ApplicationError> runWithSource(Path source) {
        final var relative = SOURCE_DIRECTORY.relativize(source);
        final var parent = relative.getParent();
        final var namespace = IntStream.range(0, parent.getNameCount())
                .mapToObj(parent::getName)
                .map(Path::toString)
                .toList();

        if (namespace.size() >= 2 && namespace.subList(0, 2).equals(List.of("magma", "java"))) {
            return Optional.empty();
        }

        final var nameWithExt = relative.getFileName().toString();
        final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        if (!Files.exists(targetParent)) {
            final var directoriesError = JavaFiles.createDirectoriesWrapped(targetParent);
            if (directoriesError.isPresent()) return directoriesError.map(JavaError::new).map(ApplicationError::new);
        }

        return JavaFiles.readStringWrapped(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .flatMapValue(input -> createRootRule().parse(input).mapErr(ApplicationError::new))
                .flatMapValue(root -> pass(root).mapErr(ApplicationError::new))
                .flatMapValue(root -> createRootRule().generate(root).mapErr(ApplicationError::new))
                .mapValue(output -> writeOutput(output, targetParent, name)).match(Function.identity(), Optional::of);
    }

    private static Result<Node, CompileError> pass(Node root) {
        return afterPass(root).orElse(new Ok<>(root));
    }

    private static Optional<Result<Node, CompileError>> afterPass(Node root) {
        if (root.is("root")) {
            final var children = root.findNodeList("children").orElse(Collections.emptyList());
            final var newChildren = children.stream()
                    .filter(child -> !child.is("package"))
                    .toList();

            return Optional.of(new Ok<>(root.withNodeList("children", newChildren)));
        }

        return Optional.empty();
    }

    private static Rule createRootRule() {
        return new TypeRule("root", createContentRule(createRootSegmentRule()));
    }

    private static Optional<ApplicationError> writeOutput(String output, Path targetParent, String name) {
        final var target = targetParent.resolve(name + ".c");
        final var header = targetParent.resolve(name + ".h");
        return JavaFiles.writeStringWrapped(target, output)
                .or(() -> JavaFiles.writeStringWrapped(header, output))
                .map(JavaError::new)
                .map(ApplicationError::new);
    }

    private static Result<List<String>, CompileError> splitByStatements(String input) {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        final var queue = IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            buffer.append(c);

            if (c == '\'') {
                final var c1 = queue.pop();
                buffer.append(c1);

                if (c1 == '\\') {
                    buffer.append(queue.pop());
                }
                buffer.append(queue.pop());
                continue;
            }

            if (c == '"') {
                while (!queue.isEmpty()) {
                    final var c1 = queue.pop();
                    buffer.append(c1);

                    if (c1 == '"') break;
                    if (c1 == '\\') {
                        buffer.append(queue.pop());
                    }
                }

                continue;
            }

            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if (c == '}' && depth == 1) {
                depth--;
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                if (c == '{' || c == '(') depth++;
                if (c == '}' || c == ')') depth--;
            }
        }
        advance(buffer, segments);

        if (depth == 0) {
            return new Ok<>(segments);
        } else {
            return new Err<>(new CompileError("Invalid depth '" + depth + "'", new StringContext(input)));
        }
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static OrRule createRootSegmentRule() {
        return new OrRule(List.of(
                createNamespacedRule("package", "package "),
                createNamespacedRule("import", "import "),
                createStructRule("class", "class "),
                createStructRule("record", "record "),
                createStructRule("interface", "interface "),
                createWhitespaceRule()
        ));
    }

    private static Rule createNamespacedRule(String type, String prefix) {
        return new TypeRule(type, new PrefixRule(prefix, new SuffixRule(new StringRule("namespace"), ";")));
    }

    private static Rule createStructRule(String type, String infix) {
        final var infixRule = new InfixRule(new StringRule("modifiers"), new FirstLocator(infix),
                new InfixRule(new StringRule("name"), new FirstLocator("{"), new StripRule(
                        new SuffixRule(createContentRule(createStructSegmentRule()), "}")
                )));
        return new TypeRule(type, infixRule);
    }

    private static Result<List<String>, CompileError> splitByValues(String input) {
        final var segments = new ArrayList<String>();
        final var buffer = new StringBuilder();
        var depth = 0;
        int i = 0;
        while (i < input.length()) {
            final var c = input.charAt(i);
            if (c == ',' && depth == 0) {
                advance(buffer, segments);
            } else {
                buffer.append(c);
                if (c == '<') depth++;
                if (c == '>') depth--;
            }
            i++;
        }

        advance(buffer, segments);
        return new Ok<>(segments);
    }

    private static Rule createStructSegmentRule() {
        return new OrRule(List.of(
                createMethodRule(),
                createInitializationRule(),
                createDefinitionStatementRule(),
                createWhitespaceRule()
        ));
    }

    private static SuffixRule createDefinitionStatementRule() {
        return new SuffixRule(createDefinitionRule(), ";");
    }

    private static Rule createInitializationRule() {
        final var infixRule = new InfixRule(new NodeRule("definition", createDefinitionRule()), new FirstLocator("="), new StripRule(new SuffixRule(new NodeRule("value", createValueRule()), ";")));
        return new TypeRule("initialization", infixRule);
    }

    private static Rule createMethodRule() {
        final var orRule = new OrRule(List.of(
                createBlockRule(createStatementRule()),
                new ExactRule(";")
        ));

        final var definition = createDefinitionRule();
        final var definitionProperty = new NodeRule("definition", definition);
        final var params = new DivideRule("params", Main::splitByValues, definition);

        final var infixRule = new InfixRule(definitionProperty, new FirstLocator("("), new InfixRule(params, new FirstLocator(")"), orRule));
        return new TypeRule("method", infixRule);
    }

    private static Rule createBlockRule(Rule statement) {
        return new StripRule(new PrefixRule("{", new SuffixRule(createContentRule(statement), "}")));
    }

    private static Rule createContentRule(Rule rule) {
        return new DivideRule("children", Main::splitByStatements, new StripRule(rule));
    }

    private static Rule createStatementRule() {
        final var valueRule = createValueRule();
        final var statement = new LazyRule();
        statement.set(new OrRule(List.of(
                createKeywordRule(),
                createDefinitionStatementRule(),
                createConditionalRule(statement, "if"),
                createConditionalRule(statement, "while"),
                createElseRule(statement),
                createInvocationRule(valueRule),
                createReturnRule(valueRule),
                createAssignmentRule(valueRule),
                createPostfixRule("post-increment", "++", valueRule),
                createPostfixRule("post-decrement", "--", valueRule),
                createWhitespaceRule()
        )));
        return statement;
    }

    private static TypeRule createKeywordRule() {
        return new TypeRule("continue", new ExactRule("continue;"));
    }

    private static TypeRule createElseRule(LazyRule statement) {
        return new TypeRule("else", new StripRule(new PrefixRule("else ", new OrRule(List.of(
                createBlockRule(statement),
                new NodeRule("value", statement)
        )))));
    }

    private static TypeRule createConditionalRule(LazyRule statement, String type) {
        final var leftRule = new StripRule(new PrefixRule("(", new NodeRule("condition", createValueRule())));
        return new TypeRule(type, new PrefixRule(type, new InfixRule(leftRule, new ParenthesesMatcher(), createBlockRule(statement))));
    }

    private static TypeRule createWhitespaceRule() {
        return new TypeRule("whitespace", new StripRule(new ExactRule("")));
    }

    private static Rule createPostfixRule(String type, String operator, Rule value) {
        return new TypeRule(type, new SuffixRule(new NodeRule("value", value), operator + ";"));
    }

    private static Rule createAssignmentRule(Rule value) {
        return new SuffixRule(new InfixRule(new NodeRule("destination", value), new FirstLocator(" "), new NodeRule("source", value)), ";");
    }

    private static Rule createInvocationRule(Rule value) {
        final var suffixRule = new SuffixRule(new InfixRule(new NodeRule("caller", value), new FirstLocator("("), new DivideRule("children", Main::splitByValues, value)), ");");
        return new TypeRule("invocation", suffixRule);
    }

    private static Rule createReturnRule(Rule value) {
        return new TypeRule("return", new StripRule(new PrefixRule("return ", new SuffixRule(new NodeRule("value", value), ";"))));
    }

    private static Rule createValueRule() {
        final var value = new LazyRule();
        value.set(new OrRule(List.of(
                createConstructionRule(value),
                createDataAccessRule(value),
                createSymbolRule(),
                createNumberRule(),
                createNotRule(value),
                createOperatorRule("greater-equals", ">=", value),
                createOperatorRule("less", "<", value),
                createOperatorRule("equals", "==", value),
                createOperatorRule("and", "&&", value),
                createCharRule()
        )));

        return value;
    }

    private static TypeRule createCharRule() {
        return new TypeRule("char", new StripRule(new PrefixRule("'", new SuffixRule(new StringRule("value"), "'"))));
    }

    private static TypeRule createNumberRule() {
        return new TypeRule("number", new StripRule(new FilterRule(new NumberFilter(), new StringRule("value"))));
    }

    private static TypeRule createOperatorRule(String type, String operator, LazyRule value) {
        return new TypeRule(type, new InfixRule(new NodeRule("left", value), new FirstLocator(operator), new NodeRule("right", value)));
    }

    private static TypeRule createNotRule(LazyRule value) {
        return new TypeRule("not", new StripRule(new PrefixRule("!", new NodeRule("value", value))));
    }

    private static TypeRule createConstructionRule(LazyRule value) {
        return new TypeRule("construction", new StripRule(new PrefixRule("new ", new InfixRule(new StringRule("type"), new FirstLocator("("), new StripRule(new SuffixRule(new DivideRule("arguments", Main::splitByValues, value), ")"))))));
    }

    private static Rule createSymbolRule() {
        return new TypeRule("symbol", new StripRule(new FilterRule(new SymbolFilter(), new StringRule(DEFAULT_VALUE))));
    }

    private static Rule createDataAccessRule(final Rule value) {
        final var rule = new InfixRule(new NodeRule("ref", value), new LastLocator("."), new StringRule("property"));
        return new TypeRule("data-access", rule);
    }

    private static Rule createDefinitionRule() {
        final var modifiers = new StringRule("modifiers");
        final var type = new StringRule("type");
        final var name = new StringRule("name");
        final var withModifiers = new InfixRule(modifiers, new LocateTypeSeparator(), type);
        final var rule = new InfixRule(new OrRule(List.of(
                withModifiers,
                type
        )), new LastLocator(" "), name);

        return new TypeRule("definition", rule);
    }
}
