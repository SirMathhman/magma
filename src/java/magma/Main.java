package magma;

import magma.java.JavaFiles;
import magma.locate.FirstLocator;
import magma.locate.LastLocator;
import magma.locate.Locator;
import magma.locate.TypeLocator;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        JavaFiles.walk(SOURCE_DIRECTORY).match(Main::compileFiles, Optional::of).ifPresent(Throwable::printStackTrace);
    }

    private static Optional<IOException> compileFiles(List<Path> files) {
        return files.stream()
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".java"))
                .map(Main::compileSource)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Optional<IOException> compileSource(Path source) {
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
            if (directoryError.isPresent()) return directoryError;
        }

        final var target = targetParent.resolve(nameWithoutExt + ".c");
        return JavaFiles.readSafe(source).mapValue(input -> {
            final var output = compileRoot(input);
            return JavaFiles.writeSafe(target, output);
        }).match(value -> value, Optional::of);
    }

    private static List<String> computeNamespace(Path parent) {
        return IntStream.range(0, parent.getNameCount())
                .mapToObj(parent::getName)
                .map(Path::toString)
                .toList();
    }

    private static String compileRoot(String root) {
        return compileAndMerge(slicesOf(Main::statementChars, root), Main::compileRootSegment, StringBuilder::append);
    }

    private static String compileAndMerge(
            List<String> segments,
            Function<String, String> compiler,
            BiFunction<StringBuilder, String, StringBuilder> merger
    ) {
        return merge(compileSegments(segments, compiler), merger);
    }

    private static String merge(
            List<String> segments,
            BiFunction<StringBuilder, String, StringBuilder> merger
    ) {
        return segments.stream().reduce(new StringBuilder(), merger, (_, next) -> next).toString();
    }

    private static List<String> compileSegments(List<String> segments, Function<String, String> compiler) {
        return segments.stream()
                .map(String::strip)
                .filter(segment -> !segment.isEmpty())
                .map(compiler)
                .toList();
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

    private static String compileRootSegment(String rootSegment) {
        return compileDisjunction("root segment", rootSegment, List.of(
                () -> compilePackage(rootSegment),
                () -> compileImport(rootSegment),
                () -> compileToStruct("class", rootSegment),
                () -> compileToStruct("record", rootSegment),
                () -> compileToStruct("interface", rootSegment)
        )).findValue().orElse("");
    }

    private static Optional<String> compileImport(String rootSegment) {
        if (rootSegment.startsWith("import")) return Optional.of("#include \"temp.h\"\n");
        return Optional.empty();
    }

    private static Optional<String> compilePackage(String rootSegment) {
        if (rootSegment.startsWith("package")) return Optional.of("");
        else return Optional.empty();
    }

    private static String invalidate(String type, String rootSegment) {
        return Results.writeErr("Invalid " + type, rootSegment, rootSegment);
    }

    private static Optional<String> compileToStruct(String keyword, String rootSegment) {
        Locator locator1 = new FirstLocator(keyword);
        return split(rootSegment, locator1).findValue().flatMap(tuple -> {
            Locator locator = new FirstLocator("{");
            return split(tuple.right(), locator).findValue().flatMap(tuple0 -> {
                return truncateRight(tuple0.right().strip(), "}").findValue().map(content -> {
                    final var outputContent = compileAndMerge(slicesOf(Main::statementChars, content), Main::compileStructSegment, StringBuilder::append);
                    return "struct " + tuple0.left().strip() + " {" + outputContent + "\n};";
                });
            });
        });
    }

    private static String compileStructSegment(String structSegment) {
        return compileDisjunction("struct segment", structSegment, List.of(
                () -> compileInitialization(structSegment, 1),
                () -> compileMethod(structSegment),
                () -> compileDefinitionStatement(structSegment, 1)
        )).findValue().orElse("");
    }

    private static Optional<String> compileDefinitionStatement(String structSegment, int depth) {
        return truncateRight(structSegment, ";").findValue().flatMap(inner -> {
            return compileDefinition(inner).map(inner0 -> generateStatement(depth, inner0));
        });
    }

    private static Optional<String> compileDefinition(String definition) {
        Locator locator = new LastLocator(" ");
        return split(definition, locator).findValue().flatMap(tuple -> {
            final var left = tuple.left().strip();
            final var inputType = split(left, new TypeLocator(' ', '>', '<')).findValue().map(Tuple::right).orElse(left);
            final var name = tuple.right().strip();

            if (!isSymbol(name)) return Optional.empty();
            return Optional.ofNullable(compileType(inputType)).map(outputType -> generateDefinition(outputType, name));
        });
    }

    private static String compileType(String type) {
        return compileDisjunction("type", type, List.of(
                () -> compileExact(type, "var", "auto"),
                () -> compileFilter(Main::isSymbol, type),
                () -> compileGeneric(type),
                () -> compileArray(type))).findValue().orElse("");
    }

    private static Optional<String> compileExact(String type, String match, String output) {
        return type.equals(match) ? Optional.of(output) : Optional.empty();
    }

    private static Optional<String> writeDebug(String category, String input) {
        Results.write(System.out, "Invalid " + category, input, input);
        return Optional.empty();
    }

    private static Optional<String> compileArray(String type) {
        return truncateRight(type, "[]").findValue().map(inner -> Optional.ofNullable(compileType(inner)).orElse("") + "[]");
    }

    private static Optional<String> compileGeneric(String type) {
        return truncateRight(type, ">").findValue().flatMap(inner -> {
            Locator locator = new FirstLocator("<");
            return split(inner, locator).findValue().map(tuple -> {
                final var caller = tuple.left();
                final var segments = slicesOf(Main::valueStrings, tuple.right());
                final var compiledSegments = compileSegments(segments, type1 -> Optional.ofNullable(compileType(type1)).orElse(""));

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

    private static Optional<String> compileFilter(Predicate<String> filter, String type) {
        return filter.test(type) ? Optional.of(type) : Optional.empty();
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

    private static Optional<String> compileMethod(String structSegment) {
        Locator locator1 = new FirstLocator(")");
        return split(structSegment, locator1).findValue().flatMap(tuple -> {
            final var beforeContent = tuple.left().strip();
            final var maybeContent = tuple.right().strip();

            Locator locator = new FirstLocator("(");
            return split(beforeContent, locator).findValue().flatMap(tuple1 -> {
                final var inputDefinition = tuple1.left();
                return compileDefinition(inputDefinition).map(definition -> {
                    final var outputContent = compileContent(maybeContent).orElse(";");
                    final var compiledParams = compileAndMerge(slicesOf(Main::valueStrings, tuple1.right()),
                            segment -> compileDefinition(segment).orElseGet(() -> invalidate("definition", segment)),
                            Main::mergeValues);

                    return "\n\t" + generateMethod(definition, compiledParams, outputContent);
                });
            });
        });
    }

    private static String generateMethod(String definition, String params, String content) {
        return definition + "(" + params + ")" + content;
    }

    private static Optional<String> compileContent(String maybeContent) {
        return truncateLeft(maybeContent, "{").findValue()
                .flatMap(inner -> truncateRight(inner, "}").findValue()
                        .map(inner0 -> "{" + compileAndMerge(slicesOf(Main::statementChars, inner0), statement -> compileStatement(statement, 2), StringBuilder::append) + "\n\t}"));
    }

    private static String compileStatement(String statement, int depth) {
        List<Supplier<Optional<String>>> compilers = List.of(
                () -> compileReturn(statement, depth),
                () -> compileCondition(statement, "if"),
                () -> compileCondition(statement, "while"),
                () -> compileElse(statement),
                () -> compileInitialization(statement, depth),
                () -> compileInvocationStatement(statement, depth),
                () -> compileDefinitionStatement(statement, depth),
                () -> compileAssignment(statement, depth)
        );

        return compileDisjunction("statement", statement, compilers).findValue().orElse("");
    }

    private static Result<String, CompileError> compileDisjunction(String type, String input, List<Supplier<Optional<String>>> compilers) {
        for (Supplier<Optional<String>> compiler : compilers) {
            final var optional = compiler.get();
            if (optional.isPresent()) {
                return new Ok<>(optional.get());
            }
        }

        return new Err<>(new CompileError("Invalid type '" + type + "'", input));
    }

    private static Optional<String> compileElse(String statement) {
        return truncateLeft(statement, "else").findValue().map(inner -> "\n\t\telse {}");
    }

    private static Optional<String> compileCondition(String statement, String prefix) {
        return truncateLeft(statement, prefix).findValue().flatMap(inner -> {
            return truncateLeft(inner.strip(), "(").findValue().flatMap(inner0 -> {
                Locator locator = new FirstLocator(")");
                return split(inner0, locator).findValue().flatMap(tuple -> {
                    return Optional.ofNullable(compileValue(tuple.left().strip())).flatMap(condition -> {
                        return compileContent(tuple.right()).map(content -> {
                            return "\n\t\t" + prefix + " (" + condition + ") " + content;
                        });
                    });
                });
            });
        });
    }

    private static Optional<String> compileInvocationStatement(String input, int depth) {
        return truncateRight(input, ";").findValue().flatMap(inner -> {
            return compileInvocation(inner).map(output -> generateStatement(depth, output));
        });
    }

    private static Optional<String> compileInvocation(String input) {
        return truncateRight(input, ")").findValue().flatMap(withoutEnd -> {
            return split(withoutEnd, new TypeLocator('(', ')', '(')).findValue().map(withoutStart -> {
                final var caller = withoutStart.left();
                final var compiled = Optional.ofNullable(compileValue(caller)).orElse(caller);
                final var compiledArgs = compileAndMerge(slicesOf(Main::valueStrings, withoutStart.right()), value -> Optional.ofNullable(compileValue(value)).orElse(value), Main::mergeValues);
                return generateInvocation(compiled, compiledArgs);
            });
        });
    }

    private static String generateInvocation(String caller, String args) {
        return caller + "(" + args + ")";
    }

    private static Optional<String> compileReturn(String statement, int depth) {
        return truncateLeft(statement, "return").findValue().flatMap(inner -> truncateRight(inner, ";").findValue().map(value -> {
            String value1 = value.strip();
            return generateStatement(depth, "return " + Optional.ofNullable(compileValue(value1)).orElse(value1));
        }));
    }

    private static Result<String, CompileError> truncateLeft(String input, String prefix) {
        if (input.startsWith(prefix)) return new Ok<>(input.substring(prefix.length()));
        return new Err<>(new CompileError("Prefix '" + prefix + "' not present", input));
    }

    private static Optional<String> compileAssignment(String statement, int depth) {
        return truncateRight(statement, ";").findValue().flatMap(inner -> {
            Locator locator = new FirstLocator("=");
            return split(inner, locator).findValue().map(inner0 -> {
                String value = inner0.left();
                final var destination = Optional.ofNullable(compileValue(value)).orElse(value);
                return generateStatement(depth, destination + " = from");
            });
        });
    }

    private static String compileValue(String value) {
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
        )).findValue().orElse("");
    }

    private static Optional<String> compileNot(String value) {
        return truncateLeft(value, "!").findValue().flatMap(inner -> Optional.ofNullable(compileValue(inner)).map(inner0 -> "!" + inner0));
    }

    private static Optional<String> compileChar(String value) {
        return truncateLeft(value, "'").findValue().flatMap(inner -> truncateRight(inner, "'").findValue().map(inner0 -> "'" + inner0 + "'"));
    }

    private static Optional<String> compileMethodAccess(String value) {
        Locator locator = new LastLocator("::");
        return split(value, locator).findValue().map(tuple -> {
            String value1 = tuple.left().strip();
            final var s = Optional.ofNullable(compileValue(value1)).orElse(value1);
            return s + "." + tuple.right().strip();
        });
    }

    private static Optional<String> compileLambda(String value) {
        return value.contains("->")
                ? Optional.of(generateMethod(generateDefinition("auto", "temp"), "", "{}"))
                : Optional.empty();
    }

    private static Optional<String> compileString(String value) {
        return truncateLeft(value, "\"").findValue().flatMap(inner -> truncateRight(inner, "\"").findValue().map(inner0 -> "\"" + inner0 + "\""));
    }

    private static Optional<String> compileOperator(String value, String operator) {
        Locator locator = new FirstLocator(operator);
        return split(value, locator).findValue().flatMap(tuple -> {
            String value1 = tuple.right().strip();
            String value2 = tuple.left().strip();
            return Optional.ofNullable(compileValue(value2)).flatMap(inner -> {
                return Optional.ofNullable(compileValue(value1)).map(inner0 -> {
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

    private static Optional<String> compileConstruction(String value) {
        return value.startsWith("new ") ? Optional.of(generateInvocation("temp", "")) : Optional.empty();
    }

    private static Optional<String> compileDataAccess(String value) {
        Locator locator = new LastLocator(".");
        return split(value, locator).findValue().flatMap(tuple -> {
            String caller = tuple.left().strip();
            return Optional.ofNullable(compileValue(caller)).map(compiled -> {
                return compiled + "." + tuple.right().strip();
            });
        });
    }

    private static Optional<String> compileInitialization(String structSegment, int depth) {
        return truncateRight(structSegment, ";").findValue().flatMap(inner -> {
            Locator locator = new FirstLocator("=");
            return split(inner, locator).findValue().flatMap(tuple -> {
                return compileDefinition(tuple.left().strip()).map(definition -> {
                    String value1 = tuple.right().strip();
                    final var value = Optional.ofNullable(compileValue(value1)).orElse(value1);
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
