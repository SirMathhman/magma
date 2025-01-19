package magma;

import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Stream;
import magma.api.stream.Streams;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.ApplicationError;
import magma.app.error.CompileError;
import magma.app.error.JavaError;
import magma.app.error.context.StringContext;
import magma.app.filter.SymbolFilter;
import magma.app.locate.FirstLocator;
import magma.app.locate.LastLocator;
import magma.app.locate.Locator;
import magma.app.rule.FilterRule;
import magma.app.rule.InfixRule;
import magma.app.rule.PrefixRule;
import magma.app.rule.Rule;
import magma.app.rule.StringRule;
import magma.app.rule.StripRule;
import magma.app.rule.SuffixRule;

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
            return splitByStatements(input).flatMapValue(segments -> compileAll(segments, new Rule() {
                @Override
                public Result<Node, CompileError> parse(String s) {
                    return compileRootSegment(s).mapValue(k -> parseString(DEFAULT_VALUE).parse(k).findValue().orElse(new MapNode()));
                }
            }).mapValue(list -> merge(list, Main::mergeStatement))).mapErr(ApplicationError::new).mapValue(output -> {
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
                .map(node -> new StringRule(DEFAULT_VALUE).parse(node).findValue().orElse(""))
                .reduce(new StringBuilder(), merger, (_, next) -> next).toString();
    }

    private static Result<List<Node>, CompileError> compileAll(List<String> segments, Rule compiler) {
        Result<List<Node>, CompileError> nodes = new Ok<>(new ArrayList<>());
        for (String segment : segments) {
            final var stripped = segment.strip();
            if (stripped.isEmpty()) continue;

            nodes = nodes.and(() -> compiler.parse(stripped)).mapValue(tuple -> {
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
            return new Err<>(new CompileError("Invalid depth '" + depth + "'", new StringContext(input)));
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
        return or("root segment", input, stream.map(supplier -> () -> supplier.get().mapValue(s -> parseString(DEFAULT_VALUE).parse(s).findValue().orElse(new MapNode())))).mapValue(node -> new StringRule(DEFAULT_VALUE).parse(node).findValue().orElse(""));
    }

    private static Result<Node, CompileError> or(String type, String input, Stream<Supplier<Result<Node, CompileError>>> stream) {
        return stream.map(Main::prepare)
                .foldLeft(Supplier::get, (current, next) -> current.or(next).mapErr(Main::merge))
                .map(result -> result.mapErr(errors -> new CompileError("Invalid " + type, new StringContext(input), errors)))
                .orElseGet(() -> new Err<>(new CompileError("No compilers present", new StringContext(input))));
    }

    private static Result<String, CompileError> compileNamespaced(String input, String prefix, String output) {
        if (input.startsWith(prefix)) return new Ok<>(output);
        return new Err<>(new CompileError("Prefix '" + prefix + "' not present.", new StringContext(input)));
    }

    private static List<CompileError> merge(Tuple<List<CompileError>, List<CompileError>> tuple) {
        final var left = tuple.left();
        final var right = tuple.right();
        final var copy = new ArrayList<>(left);
        copy.addAll(right);
        return copy;
    }

    private static Result<String, CompileError> compileToStruct(String input, String infix) {
        return InfixRule.split(new FirstLocator(infix), input).flatMapValue(tuple -> {
            return InfixRule.split(new FirstLocator("{"), tuple.right()).flatMapValue(withoutContentStart -> {
                Stream<Rule> rules = Streams.of(
                        parseSplit(parseString(DEFAULT_VALUE), new FirstLocator("("), parseSplit(parseDivide("params", Main::splitByValues, createDefinitionRule()),
                                new FirstLocator(")"),
                                parseString("after-params"))),
                        parseString(DEFAULT_VALUE)
                );
                return parseOr("root segment", rules).parse(withoutContentStart.left().strip()).flatMapValue(node -> {
                    final var stripped = withoutContentStart.right().strip();
                    return SuffixRule.truncateRight(stripped, "}").flatMapValue(content -> {
                        String name = new StringRule(DEFAULT_VALUE).parse(node).findValue().orElse("");
                        return parseDivide("children", Main::splitByStatements, new Rule() {
                            @Override
                            public Result<Node, CompileError> parse(String s) {
                                return compileStructSegment(s, name);
                            }
                        })
                                .parse(content)
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
                .map(child -> new StringRule(DEFAULT_VALUE).parse(child).findValue().orElse(""))
                .reduce(new StringBuilder(), Main::mergeStatement, (_, next) -> next);

        final var nodes = node.findNodeList("params")
                .orElse(new ArrayList<>());

        final var params = nodes.stream().map(node2 -> new InfixRule(new StringRule("type"), new FirstLocator(" "), new StringRule("name")).generate(node2).findValue().orElse("")).toList();
        final var collectorParams = String.join(", ", params);

        final var fields = params.stream()
                .map(param -> "\n\t" + param + ";")
                .collect(Collectors.joining(""));

        final var thisType = "struct " + structName;

        Node node1 = new MapNode()
                .withString("type", thisType)
                .withString("name", generateUniqueName(structName, "new"));
        final var definition = new InfixRule(new StringRule("type"), new FirstLocator(" "), new StringRule("name")).generate(node1).findValue().orElse("");

        final var thisDefinition = new MapNode().withString("type", thisType).withString("name", "this");
        final var assignments = nodes.stream()
                .map(field -> field.findString("name")).flatMap(Optional::stream)
                .map(field -> generateStatement(generateAccess("this", field) + " = " + field))
                .collect(Collectors.joining());

        final var returnThis = generateReturn(parseString(DEFAULT_VALUE).parse("this").findValue().orElse(new MapNode()));
        final var constructorBody = generateDefinitionStatement(thisDefinition) + assignments + returnThis;
        final var constructor = generateMethod(definition, collectorParams, generateBlock(constructorBody, 1));

        return "struct " + structName + " " + generateBlock(fields + constructor + joinedChildren, 0) + ";";
    }

    private static Rule parseOr(String category, Stream<Rule> rules) {
        return new Rule() {
            @Override
            public Result<Node, CompileError> parse(String input) {
                return or(category, input, rules.map(rule -> () -> rule.parse(input)));
            }
        };
    }

    private static Rule parseSplit(
            Rule leftRule,
            Locator locator,
            Rule rightRule
    ) {
        return new Rule() {
            @Override
            public Result<Node, CompileError> parse(String s) {
                return InfixRule.split(locator, s).flatMapValue(sliced -> {
                    final var leftSlice = sliced.left();
                    final var rightSlice = sliced.right();
                    return leftRule.parse(leftSlice)
                            .and(() -> rightRule.parse(rightSlice))
                            .mapValue(Tuple.merge(Node::merge));
                });
            }
        };
    }

    private static Rule parseString(String propertyKey) {
        return new Rule() {
            @Override
            public Result<Node, CompileError> parse(String s) {
                return new Ok<>(new MapNode().withString(propertyKey, s));
            }
        };
    }

    private static Rule parseDivide(
            String propertyKey,
            Function<String, Result<List<String>, CompileError>> splitter,
            Rule compiler
    ) {
        return new Rule() {
            @Override
            public Result<Node, CompileError> parse(String s) {
                return splitter.apply(s)
                        .flatMapValue(segments -> compileAll(segments, compiler))
                        .mapValue(inner -> new MapNode().withNodeList(propertyKey, inner));
            }
        };
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
                .map(supplier -> () -> supplier.get().mapValue(s -> parseString(DEFAULT_VALUE).parse(s).findValue().orElse(new MapNode()))));
    }

    private static Result<String, CompileError> compileDefinitionStatement(String structSegment) {
        return SuffixRule.truncateRight(structSegment, ";")
                .flatMapValue(definition -> createDefinitionRule().parse(definition))
                .mapValue(Main::generateDefinitionStatement);
    }

    private static String generateDefinitionStatement(Node node) {
        return generateStatement(new InfixRule(new StringRule("type"), new FirstLocator(" "), new StringRule("name")).generate(node).findValue().orElse(""));
    }

    private static Result<String, CompileError> compileInitialization(String structSegment) {
        return SuffixRule.truncateRight(structSegment, ";").flatMapValue(inner -> {
            return InfixRule.split(new FirstLocator("="), inner).flatMapValue(tuple -> {
                return createDefinitionRule().parse(tuple.left()).mapValue(node -> generateInitialization(new MapNode()
                        .withNode("definition", node)
                        .withString(DEFAULT_VALUE, "temp")));
            });
        });
    }

    private static String generateInitialization(Node node) {
        Node node1 = node.findNode("definition").orElse(new MapNode());
        final var definition = new InfixRule(new StringRule("type"), new FirstLocator(" "), new StringRule("name")).generate(node1).findValue().orElse("");
        return generateStatement(definition + " = " + ((Function<Node, Result<String, CompileError>>) new StringRule(DEFAULT_VALUE)).apply(node).findValue().orElse(""));
    }

    private static Result<String, CompileError> compileMethod(String structSegment, String structName) {
        return InfixRule.split(new FirstLocator("("), structSegment).flatMapValue(tuple -> {
            return InfixRule.split(new FirstLocator(")"), tuple.right().strip()).flatMapValue(tuple0 -> {
                final var stripped = tuple0.right().strip();
                Stream<Supplier<Result<String, CompileError>>> stream = Streams.of(() -> PrefixRule.truncateLeft(stripped, "{").flatMapValue(left -> {
                    return SuffixRule.truncateRight(left, "}").flatMapValue(content -> {
                        return splitByStatements(content).flatMapValue(segments -> compileAll(segments, new Rule() {
                            @Override
                            public Result<Node, CompileError> parse(String s) {
                                return compileStatementToNode(s);
                            }
                        }).mapValue(list -> merge(list, Main::mergeStatement))).mapValue(outputContent -> {
                            final var unwrapThis = generateInitialization(new MapNode()
                                    .withString("value", "*(struct " + structName + "*) this")
                                    .withNode("definition", new MapNode()
                                            .withString("type", "struct " + structName)
                                            .withString("name", "this")));
                            return "{" + unwrapThis + outputContent + "\n\t}";
                        });
                    });
                }), () -> stripped.equals(";") ? new Ok<>(";") : new Err<>(new CompileError("Exact string ';' was not present", new StringContext(stripped))));
                return or("root segment", stripped, stream.map(supplier -> () -> supplier.get().mapValue(s -> parseString(DEFAULT_VALUE).parse(s).findValue().orElse(new MapNode())))).mapValue(node -> new StringRule(DEFAULT_VALUE).parse(node).findValue().orElse("")).flatMapValue(content -> {
                    return createDefinitionRule().parse(tuple.left().strip())
                            .mapValue(definition -> definition.mapString("name", name -> {
                                final var actualName = name.equals(structName) ? "new" : name;
                                return generateUniqueName(structName, actualName);
                            }))
                            .mapValue(node1 -> new InfixRule(new StringRule("type"), new FirstLocator(" "), new StringRule("name")).generate(node1).findValue().orElse("")).mapValue(definition -> {
                                Node node = new MapNode()
                                        .withString("type", "void*")
                                        .withString("name", "_this_");
                                return generateMethod(definition, new InfixRule(new StringRule("type"), new FirstLocator(" "), new StringRule("name")).generate(node).findValue().orElse(""), content);
                            });
                });
            });
        });
    }

    private static String generateUniqueName(String structName, String name) {
        return structName + "_" + name;
    }

    private static Result<Node, CompileError> compileStatementToNode(String s) {
        return compileStatementToString(s).mapValue(k -> parseString(DEFAULT_VALUE).parse(k).findValue().orElse(new MapNode()));
    }

    private static String generateMethod(String definition, String params, String content) {
        return "\n\t" + definition + "(" + params + ")" + content;
    }

    private static Result<String, CompileError> compileStatementToString(String statement) {
        Stream<Supplier<Result<String, CompileError>>> stream = Streams.of(
                () -> compileInvocation(statement),
                () -> compileReturn(statement),
                () -> InfixRule.split(new FirstLocator(" "), statement).mapValue(inner -> generateStatement("temp = temp")),
                () -> SuffixRule.truncateRight(statement, "++;").mapValue(inner -> "temp++;")
        );
        return or("statement segment", statement, stream.map(supplier -> () -> supplier.get().mapValue(s -> parseString(DEFAULT_VALUE).parse(s).findValue().orElse(new MapNode())))).mapValue(node -> new StringRule(DEFAULT_VALUE).parse(node).findValue().orElse(""));
    }

    private static Result<String, CompileError> compileInvocation(String statement) {
        return SuffixRule.truncateRight(statement, ");").flatMapValue(inner -> {
            return InfixRule.split(new FirstLocator("("), inner).flatMapValue(inner0 -> {
                final var inputCaller = inner0.left();
                splitByValues(inner0.right()).flatMapValue(arguments -> compileAll(arguments, new Rule() {
                    @Override
                    public Result<Node, CompileError> parse(String s) {
                        return compileValue(s);
                    }
                }));

                return compileValue(inputCaller).mapValue(node -> new StringRule(DEFAULT_VALUE).parse(node).findValue().orElse("")).mapValue(outputCaller -> {
                    return generateStatement(outputCaller + "()");
                });
            });
        });
    }

    private static Result<String, CompileError> compileReturn(String statement) {
        return parseReturn(statement).mapValue(Main::generateReturn);
    }

    private static String generateReturn(Node value) {
        return generateStatement("return " + ((Function<Node, Result<String, CompileError>>) new StringRule(DEFAULT_VALUE)).apply(value).findValue().orElse(""));
    }

    private static Result<Node, CompileError> parseReturn(String statement) {
        return PrefixRule.truncateLeft(statement, "return ").flatMapValue(inner -> {
            return SuffixRule.truncateRight(inner, ";").flatMapValue(inputValue -> {
                return compileValue(inputValue).mapValue(node -> new StringRule(DEFAULT_VALUE).parse(node).findValue().orElse("")).mapValue(outputValue -> {
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
        if (new SymbolFilter().test(value)) {
            result = new Ok<>(value);
        } else {
            result = new Err<>(new CompileError("Not a symbol", new StringContext(value)));
        }
        return result.mapValue(inputType -> parseString(DEFAULT_VALUE).parse(inputType).findValue().orElse(new MapNode()));
    }

    private static Result<Node, CompileError> compileDataAccess(String value) {
        return InfixRule.split(new LastLocator("."), value).flatMapValue(tuple -> {
            return compileValue(tuple.left()).mapValue(node -> new StringRule(DEFAULT_VALUE).parse(node).findValue().orElse(""))
                    .mapValue(inner -> generateAccess(inner, tuple.right()));
        }).mapValue(inputType -> parseString(DEFAULT_VALUE).parse(inputType).findValue().orElse(new MapNode()));
    }

    private static String generateAccess(String reference, String property) {
        return reference + "." + property;
    }

    private static String generateStatement(String content) {
        return "\n\t\t" + content + ";";
    }

    private static Rule createDefinitionRule() {
        return parseSplit(parseOr("type", Streams.of(
                parseSplit(parseString("modifiers"), new LastLocator(" "), parseString("type")),
                parseString("type")
        )), new LastLocator(" "), new StripRule(new FilterRule(new SymbolFilter(), parseString("name"))));
    }

    private static Supplier<Result<Node, List<CompileError>>> prepare(
            Supplier<Result<Node, CompileError>> supplier
    ) {
        return () -> supplier.get().mapErr(Collections::singletonList);
    }

}
