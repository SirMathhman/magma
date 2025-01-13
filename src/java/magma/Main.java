package magma;

import magma.java.JavaFiles;
import magma.locate.FirstLocator;
import magma.locate.LastLocator;
import magma.locate.Locator;
import magma.locate.TypeLocator;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        JavaFiles.walk(SOURCE_DIRECTORY)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .match(Main::compileFiles, Optional::of).ifPresent(error -> System.err.println(error.display()));
    }

    private static Optional<ApplicationError> compileFiles(List<Path> files) {
        return files.stream()
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".java"))
                .map(Main::compileSource)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Optional<ApplicationError> compileSource(Path source) {
        final var relativized = SOURCE_DIRECTORY.relativize(source);
        final var parent = relativized.getParent();
        final var namespace = computeNamespace(parent);

        if (namespace.size() >= 2) {
            final var slice = namespace.subList(0, 2);
            if (slice.equals(List.of("magma", "java"))) {
                return Optional.empty();
            }
        }

        final var name = relativized.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));
        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        if (!Files.exists(targetParent)) {
            final var directoryError = JavaFiles.createDirectories(targetParent);
            if (directoryError.isPresent()) return directoryError.map(JavaError::new).map(ApplicationError::new);
        }

        final var target = targetParent.resolve(nameWithoutExt + ".c");
        return JavaFiles.readSafe(source).mapErr(JavaError::new).mapErr(ApplicationError::new).mapValue(input -> {
            return compileRoot(input).mapErr(ApplicationError::new).match(output -> {
                return JavaFiles.writeSafe(target, output).map(JavaError::new).map(ApplicationError::new);
            }, err -> {
                return Optional.of(err);
            });
        }).match(value -> value, Optional::of);
    }

    private static List<String> computeNamespace(Path parent) {
        return IntStream.range(0, parent.getNameCount())
                .mapToObj(parent::getName)
                .map(Path::toString)
                .toList();
    }

    private static Result<String, CompileError> compileRoot(String root) {
        return compileAndMerge(slicesOf(Main::statementChars, root), Main::compileRootSegment, StringBuilder::append);
    }

    private static Result<String, CompileError> compileAndMerge(
            List<String> segments,
            Function<String, Result<String, CompileError>> compiler,
            BiFunction<StringBuilder, String, StringBuilder> merger
    ) {
        return compileSegments(segments, compiler).mapValue(compiled -> merge(compiled, merger));
    }

    private static String merge(
            List<String> segments,
            BiFunction<StringBuilder, String, StringBuilder> merger
    ) {
        return segments.stream().reduce(new StringBuilder(), merger, (_, next) -> next).toString();
    }

    private static Result<List<String>, CompileError> compileSegments(List<String> segments, Function<String, Result<String, CompileError>> compiler) {
        return segments.stream()
                .map(String::strip)
                .filter(segment -> !segment.isEmpty())
                .map(compiler)
                .<Result<List<String>, CompileError>>reduce(new Ok<>(new ArrayList<>()), (current, element) -> current.and(() -> element).mapValue(tuple -> {
                    tuple.left().add(tuple.right());
                    return tuple.left();
                }), (_, next) -> next);
    }

    private static List<String> slicesOf(BiFunction<State, Character, State> other, String root) {
        final var queue = IntStream.range(0, root.length())
                .mapToObj(root::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        var state = new State(queue);
        while (true) {
            final var optional = state.pop().map(Tuple::right);
            if (optional.isEmpty()) break;

            final var c = optional.orElseThrow();
            state = splitAtChar(state, c, other);
        }

        final var segments = state.advance().segments;
        if (state.isLevel()) return segments;
        return Results.writeErr("Invalid depth '" + state.depth + "'", root, segments);
    }

    private static State splitAtChar(State state, Character c, BiFunction<State, Character, State> other) {
        return splitSingleQuotes(state, c)
                .or(() -> splitDoubleQuotes(state, c))
                .orElseGet(() -> other.apply(state, c));
    }

    private static Optional<State> splitDoubleQuotes(State state, char c) {
        if (c != '"') return Optional.empty();

        var current = state.append(c);
        while (true) {
            final var processed = splitDoubleQuotesChar(state);
            if (processed.isEmpty()) break;
            else current = processed.get();
        }

        return Optional.of(current);
    }

    private static Optional<State> splitDoubleQuotesChar(State state) {
        final var maybeNext = state.appendAndPop();
        if (maybeNext.isEmpty()) return Optional.empty();

        final var nextTuple = maybeNext.get();
        final var nextChar = nextTuple.right();

        if (nextChar == '"')
            return Optional.empty();
        if (nextChar == '\\') {
            return Optional.of(state.appendFromQueue().orElse(state));
        } else {
            return Optional.of(nextTuple.left());
        }
    }

    private static State statementChars(State state, char c) {
        final var appended = state.append(c);
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{' || c == '(') return appended.enter();
        if (c == '}' || c == ')') return appended.exit();
        return appended;
    }

    private static Optional<State> splitSingleQuotes(State state, char c) {
        if (c != '\'') return Optional.empty();

        return state.append(c).appendAndPop().flatMap(maybeEscapeTuple -> {
            final var escapedState = maybeEscapeTuple.left();
            final var escapedChar = maybeEscapeTuple.right();

            final var withEscaped = escapedChar == '\\'
                    ? state.appendFromQueue().orElse(escapedState)
                    : escapedState;

            return withEscaped.appendFromQueue();
        });
    }

    private static Result<String, CompileError> compileRootSegment(String rootSegment) {
        return compileDisjunction("root segment", rootSegment, List.of(
                () -> compilePackage(rootSegment),
                () -> compileImport(rootSegment),
                () -> compileToStruct("class", rootSegment),
                () -> compileToStruct("record", rootSegment),
                () -> compileToStruct("interface", rootSegment)
        ));
    }

    private static Result<String, CompileError> compileImport(String rootSegment) {
        return truncateLeft(rootSegment, "import").mapValue(inner -> "#include \"temp.h\"\n");
    }

    private static Result<String, CompileError> compilePackage(String rootSegment) {
        return truncateLeft(rootSegment, "package ").mapValue(_ -> "");
    }

    private static Result<String, CompileError> compileToStruct(String keyword, String rootSegment) {
        Locator locator1 = new FirstLocator(keyword);
        return split(rootSegment, locator1).flatMapValue(tuple -> {
            Locator locator = new FirstLocator("{");
            return split(tuple.right(), locator).flatMapValue(tuple0 -> truncateRight(tuple0.right().strip(), "}").flatMapValue(content -> {
                return compileAndMerge(slicesOf(Main::statementChars, content), Main::compileStructSegment, StringBuilder::append).mapValue(outputContent -> {
                    return "struct " + tuple0.left().strip() + " {" + outputContent + "\n};";
                });
            }));
        });
    }

    private static Result<String, CompileError> compileStructSegment(String structSegment) {
        return compileDisjunction("struct segment", structSegment, List.of(
                () -> compileInitialization(structSegment, 1),
                () -> compileMethod(structSegment),
                () -> compileDefinitionStatement(structSegment, 1)
        ));
    }

    private static Result<String, CompileError> compileDefinitionStatement(String structSegment, int depth) {
        return truncateRight(structSegment, ";").flatMapValue(inner -> compileDefinition(inner).mapValue(inner0 -> generateStatement(depth, inner0)));
    }

    private static Result<String, CompileError> compileDefinition(String definition) {
        Locator locator = new LastLocator(" ");
        return split(definition, locator).flatMapValue(tuple -> {
            final var left = tuple.left().strip();
            final var inputType = split(left, new TypeLocator(' ', '>', '<')).mapValue(Tuple::right);
            final var name = tuple.right().strip();

            if (!isSymbol(name)) return new Err<>(new CompileError("Not a name", name));
            return inputType.flatMapValue(Main::compileType).mapValue(inner -> {
                return generateDefinition(inner, name);
            });
        });
    }

    private static Result<String, CompileError> compileType(String type) {
        return compileDisjunction("type", type, List.of(
                () -> compileExact(type, "var", "auto"),
                () -> compileFilter(Main::isSymbol, type),
                () -> compileGeneric(type),
                () -> compileArray(type)));
    }

    private static Result<String, CompileError> compileExact(String input, String match, String output) {
        return input.equals(match) ? new Ok<>(output) : new Err<>(new CompileError("No exact match for '" + match + "'", input));
    }

    private static Result<String, CompileError> compileArray(String type) {
        return truncateRight(type, "[]").mapValue(inner -> Optional.of(compileType(inner).findValue().orElse("")).orElse("") + "[]");
    }

    private static Result<String, CompileError> compileGeneric(String type) {
        return truncateRight(type, ">").flatMapValue(inner -> {
            Locator locator = new FirstLocator("<");
            return split(inner, locator).flatMapValue(tuple -> {
                final var caller = tuple.left();
                final var segments = slicesOf(Main::valueStrings, tuple.right());
                return compileSegments(segments, Main::compileType).mapValue(compiledSegments -> {
                    if (caller.equals("Function") && compiledSegments.size() == 2) {
                        final var paramType = compiledSegments.get(0);
                        final var returnType = compiledSegments.get(1);
                        return "(" + paramType + " => " + returnType + ")";
                    }

                    if (caller.equals("BiFunction") && compiledSegments.size() == 3) {
                        final var firstParamType = compiledSegments.get(0);
                        final var secondParamType = compiledSegments.get(1);
                        final var returnType = compiledSegments.get(2);
                        return "((" + firstParamType + ", " + secondParamType + ") => " + returnType + ")";
                    }

                    final var compiledArgs = merge(compiledSegments, Main::mergeValues);
                    return caller + "<" + compiledArgs + ">";
                });
            });
        });
    }

    private static StringBuilder mergeValues(StringBuilder builder, String slice) {
        if (builder.isEmpty()) return builder.append(slice);
        return builder.append(", ").append(slice);
    }

    private static State valueStrings(State state, Character c) {
        if (c == ',' && state.isLevel()) return state.advance();

        final var appended = state.append(c);
        if (c == '-') {
            final var peeked = appended.peek();
            if (peeked.isPresent()) {
                if (peeked.get() == '>') {
                    return appended.appendFromQueue().orElse(appended);
                }
            }
        }

        if (c == '<' || c == '(') return appended.enter();
        if (c == '>' || c == ')') return appended.exit();
        return appended;
    }

    private static Result<String, CompileError> compileFilter(Predicate<String> filter, String type) {
        return filter.test(type) ? new Ok<>(type) : new Err<>(new CompileError("Invalid input for filter.", type));
    }

    private static boolean isSymbol(String type) {
        return IntStream.range(0, type.length())
                .mapToObj(type::charAt)
                .allMatch(ch -> Character.isLetter(ch) || ch == '_');
    }

    private static String generateDefinition(String type, String name) {
        return type + " " + name;
    }

    private static String generateStatement(int depth, String content) {
        return "\n" + "\t".repeat(depth) + content + ";";
    }

    private static Result<String, CompileError> compileMethod(String structSegment) {
        Locator locator1 = new FirstLocator(")");
        return split(structSegment, locator1).flatMapValue(tuple -> {
            final var beforeContent = tuple.left().strip();
            final var maybeContent = tuple.right().strip();

            Locator locator = new FirstLocator("(");
            return split(beforeContent, locator).flatMapValue(tuple1 -> {
                final var inputDefinition = tuple1.left();
                return compileDefinition(inputDefinition).flatMapValue(definition -> {
                    final var outputContent = compileContent(maybeContent).findValue().orElse(";");

                    return compileAndMerge(slicesOf(Main::valueStrings, tuple1.right()),
                            Main::compileDefinition,
                            Main::mergeValues)
                            .mapValue(compiledParams -> "\n\t" + generateMethod(definition, compiledParams, outputContent));
                });
            });
        });
    }

    private static String generateMethod(String definition, String params, String content) {
        return definition + "(" + params + ")" + content;
    }

    private static Result<String, CompileError> compileContent(String maybeContent) {
        return truncateLeft(maybeContent, "{").flatMapValue(inner ->
                truncateRight(inner, "}").mapValue(inner0 -> "{" + compileAndMerge(slicesOf(Main::statementChars, inner0), statement ->
                        compileStatement(statement, 2), StringBuilder::append) + "\n\t}"));
    }

    private static Result<String, CompileError> compileStatement(String statement, int depth) {
        return compileDisjunction("statement", statement, List.of(
                () -> compileReturn(statement, depth),
                () -> compileCondition(statement, "if"),
                () -> compileCondition(statement, "while"),
                () -> compileElse(statement),
                () -> compileInitialization(statement, depth),
                () -> compileInvocationStatement(statement, depth),
                () -> compileDefinitionStatement(statement, depth),
                () -> compileAssignment(statement, depth)
        ));
    }

    private static Result<String, CompileError> compileDisjunction(String type, String input, List<Supplier<Result<String, CompileError>>> compilers) {
        var errors = new ArrayList<CompileError>();
        for (var compiler : compilers) {
            final var result = compiler.get();
            if (result.isOk()) {
                return result;
            } else {
                errors.add(result.findError().orElseThrow());
            }
        }

        return new Err<>(new CompileError("Invalid type '" + type + "'", input, errors));
    }

    private static Result<String, CompileError> compileElse(String statement) {
        return truncateLeft(statement, "else").mapValue(inner -> "\n\t\telse {}");
    }

    private static Result<String, CompileError> compileCondition(String statement, String prefix) {
        return truncateLeft(statement, prefix).flatMapValue(inner -> {
            return truncateLeft(inner.strip(), "(").flatMapValue(inner0 -> {
                Locator locator = new FirstLocator(")");
                return split(inner0, locator).flatMapValue(tuple -> {
                    return compileValue(tuple.left().strip()).flatMapValue(condition -> {
                        return compileContent(tuple.right()).mapValue(content -> {
                            return "\n\t\t" + prefix + " (" + condition + ") " + content;
                        });
                    });
                });
            });
        });
    }

    private static Result<String, CompileError> compileInvocationStatement(String input, int depth) {
        return truncateRight(input, ";").flatMapValue(inner -> {
            return compileInvocation(inner).mapValue(output -> generateStatement(depth, output));
        });
    }

    private static Result<String, CompileError> compileInvocation(String input) {
        return truncateRight(input, ")").flatMapValue(withoutEnd -> {
            return split(withoutEnd, new TypeLocator('(', ')', '(')).flatMapValue(withoutStart -> {
                final var caller = withoutStart.left();
                final var compiled = Optional.of(compileValue(caller).findValue().orElse("")).orElse(caller);
                final var segments = slicesOf(Main::valueStrings, withoutStart.right());
                return compileAndMerge(segments, magma.Main::compileValue, Main::mergeValues).mapValue(compiledArgs -> {
                    return generateInvocation(compiled, compiledArgs);
                });
            });
        });
    }

    private static String generateInvocation(String caller, String args) {
        return caller + "(" + args + ")";
    }

    private static Result<String, CompileError> compileReturn(String statement, int depth) {
        return truncateLeft(statement, "return").flatMapValue(inner -> truncateRight(inner, ";").mapValue(value -> {
            String value1 = value.strip();
            return generateStatement(depth, "return " + Optional.of(compileValue(value1).findValue().orElse("")).orElse(value1));
        }));
    }

    private static Result<String, CompileError> truncateLeft(String input, String prefix) {
        if (input.startsWith(prefix)) return new Ok<>(input.substring(prefix.length()));
        return new Err<>(new CompileError("Prefix '" + prefix + "' not present", input));
    }

    private static Result<String, CompileError> compileAssignment(String statement, int depth) {
        return truncateRight(statement, ";").flatMapValue(inner -> {
            Locator locator = new FirstLocator("=");
            return split(inner, locator).mapValue(inner0 -> {
                String value = inner0.left();
                final var destination = Optional.of(compileValue(value).findValue().orElse("")).orElse(value);
                return generateStatement(depth, destination + " = from");
            });
        });
    }

    private static Result<String, CompileError> compileValue(String value) {
        return compileDisjunction("value", value, List.of(
                () -> compileString(value),
                () -> compileChar(value),
                () -> compileConstruction(value),
                () -> compileInvocation(value),
                () -> compileLambda(value),
                () -> compileDataAccess(value),
                () -> compileOperator(value, "+"),
                () -> compileOperator(value, "=="),
                () -> compileOperator(value, "!="),
                () -> compileOperator(value, "&&"),
                () -> compileMethodAccess(value),
                () -> compileFilter(Main::isSymbol, value),
                () -> compileFilter(Main::isNumber, value),
                () -> compileNot(value)
        ));
    }

    private static Result<String, CompileError> compileNot(String value) {
        return truncateLeft(value, "!").flatMapValue(inner -> compileValue(inner).mapValue(inner0 -> "!" + inner0));
    }

    private static Result<String, CompileError> compileChar(String value) {
        return truncateLeft(value, "'").flatMapValue(inner -> truncateRight(inner, "'").mapValue(inner0 -> "'" + inner0 + "'"));
    }

    private static Result<String, CompileError> compileMethodAccess(String value) {
        Locator locator = new LastLocator("::");
        return split(value, locator).mapValue(tuple -> {
            String value1 = tuple.left().strip();
            final var s = Optional.of(compileValue(value1).findValue().orElse("")).orElse(value1);
            return s + "." + tuple.right().strip();
        });
    }

    private static Result<String, CompileError> compileLambda(String value) {
        return split(value, new FirstLocator("->")).mapValue(_ -> generateMethod(generateDefinition("auto", "temp"), "", "{}"));
    }

    private static Result<String, CompileError> compileString(String value) {
        return truncateLeft(value, "\"").flatMapValue(inner -> truncateRight(inner, "\"").mapValue(inner0 -> "\"" + inner0 + "\""));
    }

    private static Result<String, CompileError> compileOperator(String value, String operator) {
        Locator locator = new FirstLocator(operator);
        return split(value, locator).flatMapValue(tuple -> {
            String value1 = tuple.right().strip();
            String value2 = tuple.left().strip();
            return compileValue(value2).flatMapValue(inner -> {
                return compileValue(value1).mapValue(inner0 -> {
                    return inner + " " + operator + " " + inner0;
                });
            });
        });
    }

    private static boolean isNumber(String input) {
        return IntStream.range(0, input.length())
                .mapToObj(index -> new Tuple<>(index, input.charAt(index)))
                .allMatch(tuple -> (tuple.left() == 0 && tuple.right() == '-') || Character.isDigit(tuple.right()));
    }

    private static Result<String, CompileError> compileConstruction(String value) {
        return truncateLeft(value, "new ").mapValue(_ -> generateInvocation("temp", ""));
    }

    private static Result<String, CompileError> compileDataAccess(String value) {
        Locator locator = new LastLocator(".");
        return split(value, locator).flatMapValue(tuple -> {
            String caller = tuple.left().strip();
            return compileValue(caller).mapValue(compiled -> {
                return compiled + "." + tuple.right().strip();
            });
        });
    }

    private static Result<String, CompileError> compileInitialization(String structSegment, int depth) {
        return truncateRight(structSegment, ";").flatMapValue(inner -> {
            Locator locator = new FirstLocator("=");
            return split(inner, locator).flatMapValue(tuple -> {
                return compileDefinition(tuple.left().strip()).mapValue(definition -> {
                    String value1 = tuple.right().strip();
                    final var value = Optional.of(compileValue(value1).findValue().orElse("")).orElse(value1);
                    return generateStatement(depth, definition + " = " + value);
                });
            });
        });
    }

    private static Result<String, CompileError> truncateRight(String input, String slice) {
        if (input.endsWith(slice)) return new Ok<>(input.substring(0, input.length() - slice.length()));
        return new Err<>(new CompileError("Suffix '" + slice + "' not present", input));
    }

    private static Result<Tuple<String, String>, CompileError> split(String input, Locator locator) {
        return locator.locate(input).<Result<Tuple<String, String>, CompileError>>map(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + locator.computeLength());
            return new Ok<>(new Tuple<>(left, right));
        }).orElseGet(() -> new Err<>(new CompileError(locator.createErrorMessage(), input)));
    }
}
