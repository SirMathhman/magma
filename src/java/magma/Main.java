package magma;

import magma.error.ApplicationError;
import magma.error.CompileError;
import magma.error.JavaError;
import magma.locate.FirstLocator;
import magma.locate.LastLocator;
import magma.locate.Locator;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.rule.FilterRule;
import magma.stream.Stream;
import magma.stream.Streams;

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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
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
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());
            return new Ok<>(sources);
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Optional<ApplicationError> runWithSources(Set<Path> sources) {
        for (Path source : sources) {
            final var error = runWithSource(source);
            if (error.isPresent()) return error;
        }
        return Optional.empty();
    }

    private static Optional<ApplicationError> runWithSource(Path source) {
        final var relative = SOURCE_DIRECTORY.relativize(source);
        final var parent = relative.getParent();
        final var nameWithExt = relative.getFileName().toString();
        final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        if (!Files.exists(targetParent)) {
            final var directoriesError = createDirectoriesWrapped(targetParent);
            if (directoriesError.isPresent()) return directoriesError.map(JavaError::new).map(ApplicationError::new);
        }

        return readStringWrapped(source).mapErr(JavaError::new).mapErr(ApplicationError::new).mapValue(input -> {
            return splitByStatements(input).flatMapValue(segments -> compileAll(segments, s -> compileRootSegment(s).mapValue(k -> createDefaultNode(k))).mapValue(list -> merge(list, Main::mergeStatement))).mapErr(ApplicationError::new).mapValue(output -> {
                final var target = targetParent.resolve(name + ".c");
                final var header = targetParent.resolve(name + ".h");
                return writeStringWrapped(target, output)
                        .or(() -> writeStringWrapped(header, output))
                        .map(JavaError::new)
                        .map(ApplicationError::new);
            }).match(Function.identity(), Optional::of);
        }).match(Function.identity(), Optional::of);
    }

    private static Result<String, IOException> readStringWrapped(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Optional<IOException> createDirectoriesWrapped(Path targetParent) {
        try {
            Files.createDirectories(targetParent);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    private static Optional<IOException> writeStringWrapped(Path target, String output) {
        try {
            Files.writeString(target, output);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    private static String merge(List<Node> nodes, BiFunction<StringBuilder, String, StringBuilder> merger) {
        return nodes.stream()
                .map(node -> generateWithDefaultValue(node))
                .reduce(new StringBuilder(), merger, (_, next) -> next).toString();
    }

    private static Result<List<Node>, CompileError> compileAll(List<String> segments, Function<String, Result<Node, CompileError>> compiler) {
        Result<List<Node>, CompileError> nodes = new Ok<>(new ArrayList<>());
        for (String segment : segments) {
            final var stripped = segment.strip();
            if (stripped.isEmpty()) continue;

            nodes = nodes.and(() -> compiler.apply(stripped)).mapValue(tuple -> {
                tuple.left().add(tuple.right());
                return tuple.left();
            });
        }
        return nodes;
    }

    private static StringBuilder mergeStatement(StringBuilder builder, String element) {
        return builder.append(element);
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
            return new Err<>(new CompileError("Invalid depth '" + depth + "'", input));
        }
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static Result<String, CompileError> compileRootSegment(String input) {
        Stream<Supplier<Result<String, CompileError>>> stream = Streams.of(
                () -> compileNamespaced(input, "package ", ""),
                () -> compileNamespaced(input, "import ", "#include <temp.h>\n"),
                () -> compileToStruct(input, "class "),
                () -> compileToStruct(input, "record "),
                () -> compileToStruct(input, "interface ")
        );
        return or("root segment", input, stream.map(supplier -> () -> supplier.get().mapValue(s -> createDefaultNode(s)))).mapValue(node -> generateWithDefaultValue(node));
    }

    private static Result<Node, CompileError> or(String type, String input, Stream<Supplier<Result<Node, CompileError>>> stream) {
        return stream.map(Main::prepare)
                .foldLeft(Supplier::get, (current, next) -> current.or(next).mapErr(Main::merge))
                .map(result -> result.mapErr(errors -> new CompileError("Invalid " + type, input, errors)))
                .orElseGet(() -> new Err<>(new CompileError("No compilers present", input)));
    }

    private static Result<String, CompileError> compileNamespaced(String input, String prefix, String output) {
        if (input.startsWith(prefix)) return new Ok<>(output);
        return new Err<>(new CompileError("Prefix '" + prefix + "' not present.", input));
    }

    private static List<CompileError> merge(Tuple<List<CompileError>, List<CompileError>> tuple) {
        final var left = tuple.left();
        final var right = tuple.right();
        final var copy = new ArrayList<>(left);
        copy.addAll(right);
        return copy;
    }

    private static Result<String, CompileError> compileToStruct(String input, String infix) {
        return split(new FirstLocator(infix), input).flatMapValue(tuple -> {
            return split(new FirstLocator("{"), tuple.right()).flatMapValue(withoutContentStart -> {
                Stream<Function<String, Result<Node, CompileError>>> rules = Streams.of(
                        parseSplit(parseString(DEFAULT_VALUE), new FirstLocator("("), parseSplit(parseDivide("params", Main::splitByValues, definition -> createDefinitionRule().apply(definition)),
                                new FirstLocator(")"),
                                parseString("after-params"))),
                        parseString(DEFAULT_VALUE)
                );
                return parseOr("root segment", rules).apply(withoutContentStart.left().strip()).flatMapValue(node -> {
                    final var stripped = withoutContentStart.right().strip();
                    return truncateRight(stripped, "}").flatMapValue(content -> {
                        String name = generateWithDefaultValue(node);
                        return parseDivide("children", Main::splitByStatements, segment -> compileStructSegment(segment, name))
                                .apply(content)
                                .mapValue(other -> modifyAndGenerateStruct(name, node.merge(other)));
                    });
                });
            });
        });
    }

    private static String modifyAndGenerateStruct(String structName, Node node) {
        final var joinedChildren = node.findNodeList("children")
                .orElse(new ArrayList<>())
                .stream()
                .map(child -> generateWithDefaultValue(child))
                .reduce(new StringBuilder(), Main::mergeStatement, (_, next) -> next);

        final var nodes = node.findNodeList("params")
                .orElse(new ArrayList<>());

        final var params = nodes.stream().map(Main::generateDefinition).toList();
        final var collectorParams = String.join(", ", params);

        final var fields = params.stream()
                .map(param -> "\n\t" + param + ";")
                .collect(Collectors.joining(""));

        final var thisType = "struct " + structName;

        final var definition = generateDefinition(new MapNode()
                .withString("type", thisType)
                .withString("name", generateUniqueName(structName, "new")));

        final var thisDefinition = new MapNode().withString("type", thisType).withString("name", "this");
        final var assignments = nodes.stream()
                .map(field -> field.findString("name")).flatMap(Optional::stream)
                .map(field -> generateStatement(generateAccess("this", field) + " = " + field))
                .collect(Collectors.joining());

        final var returnThis = generateReturn(createDefaultNode("this"));
        final var constructorBody = generateDefinitionStatement(thisDefinition) + assignments + returnThis;
        final var constructor = generateMethod(definition, collectorParams, generateBlock(constructorBody, 1));

        return "struct " + structName + " " + generateBlock(fields + constructor + joinedChildren, 0) + ";";
    }

    private static Function<String, Result<Node, CompileError>> parseOr(String category, Stream<Function<String, Result<Node, CompileError>>> rules) {
        return input -> or(category, input, rules.map(rule -> () -> rule.apply(input)));
    }

    private static Function<String, Result<Node, CompileError>> parseSplit(
            Function<String, Result<Node, CompileError>> leftRule,
            Locator locator,
            Function<String, Result<Node, CompileError>> rightRule
    ) {
        return input -> split(locator, input).flatMapValue(sliced -> {
            final var leftSlice = sliced.left();
            final var rightSlice = sliced.right();
            return leftRule.apply(leftSlice)
                    .and(() -> rightRule.apply(rightSlice))
                    .mapValue(Tuple.merge(Node::merge));
        });
    }

    private static Function<String, Result<Node, CompileError>> parseString(String propertyKey) {
        return input -> new Ok<>(new MapNode().withString(propertyKey, input));
    }

    private static Function<String, Result<Node, CompileError>> parseDivide(
            String propertyKey,
            Function<String, Result<List<String>, CompileError>> splitter,
            Function<String, Result<Node, CompileError>> compiler
    ) {
        return input -> splitter.apply(input)
                .flatMapValue(segments -> compileAll(segments, compiler))
                .mapValue(inner -> new MapNode().withNodeList(propertyKey, inner));
    }

    private static String generateBlock(String content, int depth) {
        return "{" + content + "\n" +
               "\t".repeat(depth) +
               "}";
    }

    private static Result<List<String>, CompileError> splitByValues(String input) {
        final var segments = new ArrayList<String>();
        final var buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            final var c = input.charAt(i);
            if (c == ',') {
                advance(buffer, segments);
            } else {
                buffer.append(c);
            }
        }

        advance(buffer, segments);
        return new Ok<>(segments);
    }

    private static Result<Node, CompileError> compileStructSegment(String structSegment, String structName) {
        Stream<Supplier<Result<String, CompileError>>> stream = Streams.of(
                () -> compileMethod(structSegment, structName),
                () -> compileInitialization(structSegment),
                () -> compileDefinitionStatement(structSegment)
        );
        return or("struct segment", structSegment, stream
                .map(supplier -> () -> supplier.get().mapValue(s -> createDefaultNode(s))));
    }

    private static Result<String, CompileError> compileDefinitionStatement(String structSegment) {
        return truncateRight(structSegment, ";")
                .flatMapValue(definition -> createDefinitionRule().apply(definition))
                .mapValue(Main::generateDefinitionStatement);
    }

    private static String generateDefinitionStatement(Node node) {
        return generateStatement(generateDefinition(node));
    }

    private static Result<String, CompileError> compileInitialization(String structSegment) {
        return truncateRight(structSegment, ";").flatMapValue(inner -> {
            return split(new FirstLocator("="), inner).flatMapValue(tuple -> {
                return createDefinitionRule().apply(tuple.left()).mapValue(node -> generateInitialization(new MapNode()
                        .withNode("definition", node)
                        .withString(DEFAULT_VALUE, "temp")));
            });
        });
    }

    private static String generateInitialization(Node node) {
        final var definition = generateDefinition(node.findNode("definition").orElse(new MapNode()));
        return generateStatement(definition + " = " + node.findString(DEFAULT_VALUE).orElse(""));
    }

    private static Result<String, CompileError> compileMethod(String structSegment, String structName) {
        return split(new FirstLocator("("), structSegment).flatMapValue(tuple -> {
            return split(new FirstLocator(")"), tuple.right().strip()).flatMapValue(tuple0 -> {
                final var stripped = tuple0.right().strip();
                Stream<Supplier<Result<String, CompileError>>> stream = Streams.of(() -> truncateLeft(stripped, "{").flatMapValue(left -> {
                    return truncateRight(left, "}").flatMapValue(content -> {
                        return splitByStatements(content).flatMapValue(segments -> compileAll(segments, Main::compileStatementToNode)
                                .mapValue(list -> merge(list, Main::mergeStatement))).mapValue(outputContent -> {
                            final var unwrapThis = generateInitialization(new MapNode()
                                    .withString("value", "*(struct " + structName + "*) this")
                                    .withNode("definition", new MapNode()
                                            .withString("type", "struct " + structName)
                                            .withString("name", "this")));
                            return "{" + unwrapThis + outputContent + "\n\t}";
                        });
                    });
                }), () -> stripped.equals(";") ? new Ok<>(";") : new Err<>(new CompileError("Exact string ';' was not present", stripped)));
                return or("root segment", stripped, stream.map(supplier -> () -> supplier.get().mapValue(s -> createDefaultNode(s)))).mapValue(node -> generateWithDefaultValue(node)).flatMapValue(content -> {
                    return createDefinitionRule().apply(tuple.left().strip())
                            .mapValue(definition -> definition.mapString("name", name -> {
                                final var actualName = name.equals(structName) ? "new" : name;
                                return generateUniqueName(structName, actualName);
                            }))
                            .mapValue(Main::generateDefinition).mapValue(definition -> {
                                return generateMethod(definition, generateDefinition(new MapNode()
                                        .withString("type", "void*")
                                        .withString("name", "_this_")), content);
                            });
                });
            });
        });
    }

    private static String generateUniqueName(String structName, String name) {
        return structName + "_" + name;
    }

    private static Result<Node, CompileError> compileStatementToNode(String s) {
        return compileStatementToString(s).mapValue(k -> createDefaultNode(k));
    }

    private static String generateMethod(String definition, String params, String content) {
        return "\n\t" + definition + "(" + params + ")" + content;
    }

    private static Result<String, CompileError> compileStatementToString(String statement) {
        Stream<Supplier<Result<String, CompileError>>> stream = Streams.of(
                () -> compileInvocation(statement),
                () -> compileReturn(statement),
                () -> split(new FirstLocator(" "), statement).mapValue(inner -> generateStatement("temp = temp")),
                () -> truncateRight(statement, "++;").mapValue(inner -> "temp++;")
        );
        return or("statement segment", statement, stream.map(supplier -> () -> supplier.get().mapValue(s -> createDefaultNode(s)))).mapValue(node -> generateWithDefaultValue(node));
    }

    private static Result<String, CompileError> compileInvocation(String statement) {
        return truncateRight(statement, ");").flatMapValue(inner -> {
            return split(new FirstLocator("("), inner).flatMapValue(inner0 -> {
                final var inputCaller = inner0.left();
                splitByValues(inner0.right()).flatMapValue(arguments -> compileAll(arguments, Main::compileValue));

                return compileValue(inputCaller).mapValue(node -> generateWithDefaultValue(node)).mapValue(outputCaller -> {
                    return generateStatement(outputCaller + "()");
                });
            });
        });
    }

    private static Result<String, CompileError> compileReturn(String statement) {
        return parseReturn(statement).mapValue(Main::generateReturn);
    }

    private static String generateReturn(Node value) {
        return generateStatement("return " + value.findString(DEFAULT_VALUE).orElse(""));
    }

    private static Result<Node, CompileError> parseReturn(String statement) {
        return truncateLeft(statement, "return ").flatMapValue(inner -> {
            return truncateRight(inner, ";").flatMapValue(inputValue -> {
                return compileValue(inputValue).mapValue(node -> generateWithDefaultValue(node)).mapValue(outputValue -> {
                    return new MapNode().withString("value", outputValue);
                });
            });
        });
    }

    private static Result<Node, CompileError> compileValue(String value) {
        return or("value", value, Streams.of(
                () -> compileDataAccess(value),
                () -> compileSymbol(value)
        ));
    }

    private static Result<Node, CompileError> compileSymbol(String value) {
        Result<String, CompileError> result;
        if (isSymbol(value)) {
            result = new Ok<>(value);
        } else {
            result = new Err<>(new CompileError("Not a symbol", value));
        }
        return result.mapValue(s -> createDefaultNode(s));
    }

    private static Result<Node, CompileError> compileDataAccess(String value) {
        return split(new LastLocator("."), value).flatMapValue(tuple -> {
            return compileValue(tuple.left()).mapValue(node -> generateWithDefaultValue(node))
                    .mapValue(inner -> generateAccess(inner, tuple.right()));
        }).mapValue(s -> createDefaultNode(s));
    }

    private static String generateAccess(String reference, String property) {
        return reference + "." + property;
    }

    private static String generateStatement(String content) {
        return "\n\t\t" + content + ";";
    }

    private static Result<String, CompileError> truncateLeft(String input, String slice) {
        if (input.startsWith(slice)) return new Ok<>(input.substring(slice.length()));
        return new Err<>(new CompileError("Prefix '" + slice + "' not present", input));
    }

    private static String generateDefinition(Node node) {
        final var type = node.findString("type").orElse("");
        final var name = node.findString("name").orElse("");
        return generateDefinition(type, name);
    }

    private static Function<String, Result<Node, CompileError>> createDefinitionRule() {
        return parseSplit(parseOr("type", Streams.of(
                parseSplit(parseString("modifiers"), new LastLocator(" "), parseString("type")),
                parseString("type")
        )), new LastLocator(" "), parseStrip(new FilterRule(Main::isSymbol, parseString("name"))));
    }

    private static Function<String, Result<Node, CompileError>> parseStrip(Function<String, Result<Node, CompileError>> childRule) {
        return input -> childRule.apply(input.strip());
    }

    private static Node createDefaultNode(String inputType) {
        return new MapNode().withString(DEFAULT_VALUE, inputType);
    }

    private static String generateWithDefaultValue(Node node) {
        return node.findString(DEFAULT_VALUE).orElse("");
    }

    private static boolean isSymbol(String input) {
        for (int i = 0; i < input.length(); i++) {
            final var c = input.charAt(i);
            if (Character.isLetter(c)) continue;
            return false;
        }
        return true;
    }

    private static String generateDefinition(String type, String name) {
        return type + " " + name;
    }

    private static Result<String, CompileError> truncateRight(String input, String slice) {
        if (input.endsWith(slice)) {
            return new Ok<>(input.substring(0, input.length() - slice.length()));
        } else {
            return new Err<>(new CompileError("Suffix '" + slice + "' not present", input));
        }
    }

    private static Result<Tuple<String, String>, CompileError> split(Locator locator, String input) {
        return locator.locate(input).<Result<Tuple<String, String>, CompileError>>map(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + locator.length());
            final var tuple = new Tuple<>(left, right);
            return new Ok<>(tuple);
        }).orElseGet(() -> new Err<>(new CompileError("Infix '" + locator.unwrap() + "' not present", input)));
    }

    private static Supplier<Result<Node, List<CompileError>>> prepare(
            Supplier<Result<Node, CompileError>> supplier
    ) {
        return () -> supplier.get().mapErr(Collections::singletonList);
    }
}
