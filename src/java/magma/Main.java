package magma;

import magma.java.JavaFiles;
import magma.locate.FirstLocator;
import magma.locate.LastLocator;
import magma.locate.Locator;
import magma.locate.TypeLocator;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
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
        return writeError(System.err, "Invalid depth '" + state.depth + "'", root, segments);
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
        if (rootSegment.startsWith("package")) return "";
        if (rootSegment.startsWith("import")) return "#include \"temp.h\"\n";

        return compileToStruct("class", rootSegment)
                .or(() -> compileToStruct("record", rootSegment))
                .or(() -> compileToStruct("interface", rootSegment))
                .orElseGet(() -> invalidate("root segment", rootSegment));
    }

    private static String invalidate(String type, String rootSegment) {
        return writeError(System.err, "Invalid " + type, rootSegment, rootSegment);
    }

    private static <T> T writeError(PrintStream stream, String message, String rootSegment, T value) {
        stream.println(message + ": " + rootSegment);
        return value;
    }

    private static Optional<String> compileToStruct(String keyword, String rootSegment) {
        return split(rootSegment, new FirstLocator(keyword)).flatMap(tuple -> {
            return split(tuple.right(), new FirstLocator("{")).flatMap(tuple0 -> {
                return truncateRight(tuple0.right().strip(), "}").map(content -> {
                    final var outputContent = compileAndMerge(slicesOf(Main::statementChars, content), Main::compileStructSegment, StringBuilder::append);
                    return "struct " + tuple0.left().strip() + " {" + outputContent + "\n};";
                });
            });
        });
    }

    private static String compileStructSegment(String structSegment) {
        return compileInitialization(structSegment, 1)
                .or(() -> compileMethod(structSegment))
                .or(() -> compileDefinitionStatement(structSegment, 1))
                .orElseGet(() -> invalidate("struct segment", structSegment));
    }

    private static Optional<String> compileDefinitionStatement(String structSegment, int depth) {
        return truncateRight(structSegment, ";").flatMap(inner -> {
            return compileDefinition(inner).map(inner0 -> generateStatement(depth, inner0));
        });
    }

    private static Optional<String> compileDefinition(String definition) {
        return split(definition, new LastLocator(" ")).flatMap(tuple -> {
            final var left = tuple.left().strip();
            final var inputType = split(left, new TypeLocator()).map(Tuple::right).orElse(left);
            final var name = tuple.right().strip();

            if (!isSymbol(name)) return Optional.empty();
            return compileType(inputType).map(outputType -> generateDefinition(outputType, name));
        });
    }

    private static Optional<String> compileType(String type) {
        final var optional = compileExact(type, "var", "auto")
                .or(() -> compileFilter(Main::isSymbol, type))
                .or(() -> compileGeneric(type))
                .or(() -> compileArray(type));

        if (optional.isPresent()) return optional;
        return writeDebug(type);
    }

    private static Optional<String> compileExact(String type, String match, String output) {
        return type.equals(match) ? Optional.of(output) : Optional.empty();
    }

    private static Optional<String> writeDebug(String type) {
        writeError(System.out, "Invalid type", type, type);
        return Optional.empty();
    }

    private static Optional<String> compileArray(String type) {
        return truncateRight(type, "[]").map(inner -> compileType(inner).orElse("") + "[]");
    }

    private static Optional<String> compileGeneric(String type) {
        return truncateRight(type, ">").flatMap(inner -> split(inner, new FirstLocator("<")).map(tuple -> {
            final var caller = tuple.left();
            final var segments = slicesOf(Main::valueStrings, tuple.right());
            final var compiledSegments = compileSegments(segments, type1 -> compileType(type1).orElse(""));

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
        }));
    }

    private static StringBuilder mergeValues(StringBuilder builder, String slice) {
        if (builder.isEmpty()) return builder.append(slice);
        return builder.append(", ").append(slice);
    }

    private static State valueStrings(State state, Character c) {
        if (c == ',' && state.isLevel()) return state.advance();

        final var appended = state.append(c);
        if (c == '<') return appended.enter();
        if (c == '>') return appended.exit();
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
        return split(structSegment, new FirstLocator(")")).flatMap(tuple -> {
            final var beforeContent = tuple.left().strip();
            final var maybeContent = tuple.right().strip();

            return split(beforeContent, new FirstLocator("(")).flatMap(tuple1 -> {
                final var inputDefinition = tuple1.left();
                return compileDefinition(inputDefinition).map(definition -> {
                    final var outputContent = compileContent(maybeContent).orElse(";");
                    final var compiledParams = compileAndMerge(slicesOf(Main::valueStrings, tuple1.right()),
                            segment -> compileDefinition(segment).orElseGet(() -> invalidate("definition", segment)),
                            Main::mergeValues);

                    return "\n\t" + definition + "(" + compiledParams + ")" + outputContent;
                });
            });
        });
    }

    private static Optional<String> compileContent(String maybeContent) {
        return truncateLeft(maybeContent, "{")
                .flatMap(inner -> truncateRight(inner, "}")
                        .map(inner0 -> "{" + compileAndMerge(slicesOf(Main::statementChars, inner0), statement -> compileStatement(statement, 2), StringBuilder::append) + "\n\t}"));
    }

    private static String compileStatement(String statement, int depth) {
        return compileReturn(statement, depth)
                .or(() -> compileCondition(statement, "if"))
                .or(() -> compileCondition(statement, "while"))
                .or(() -> compileElse(statement))
                .or(() -> compileInitialization(statement, depth))
                .or(() -> compileInvocationStatement(statement, depth))
                .or(() -> compileDefinitionStatement(statement, depth))
                .or(() -> compileAssignment(statement, depth))
                .orElseGet(() -> invalidate("statement", statement));
    }

    private static Optional<String> compileElse(String statement) {
        return truncateLeft(statement, "else").map(inner -> "\n\t\telse {}");
    }

    private static Optional<String> compileCondition(String statement, String prefix) {
        return truncateLeft(statement, prefix).map(inner -> "\n\t\t" + prefix + " (1) {}");
    }

    private static Optional<String> compileInvocationStatement(String input, int depth) {
        return truncateRight(input, ";").flatMap(inner -> {
            return compileInvocation(inner).map(output -> generateStatement(depth, output));
        });
    }

    private static Optional<String> compileInvocation(String input) {
        return split(input.strip(), new FirstLocator("(")).flatMap(inner -> truncateRight(inner.right(), ")").map(withoutEnd -> {
            final var caller = inner.left();
            final var compiled = compileValue(caller);
            final var compiledArgs = compileAndMerge(slicesOf(Main::valueStrings, withoutEnd), Main::compileValue, Main::mergeValues);
            return generateInvocation(compiled, compiledArgs);
        }));
    }

    private static String generateInvocation(String caller, String args) {
        return caller + "(" + args + ")";
    }

    private static Optional<String> compileReturn(String statement, int depth) {
        return truncateLeft(statement, "return").flatMap(inner -> truncateRight(inner, ";").map(value -> {
            return generateStatement(depth, "return " + compileValue(value.strip()));
        }));
    }

    private static Optional<String> truncateLeft(String input, String slice) {
        return input.startsWith(slice) ? Optional.of(input.substring(slice.length())) : Optional.empty();
    }

    private static Optional<String> compileAssignment(String statement, int depth) {
        return truncateRight(statement, ";").flatMap(inner -> {
            return split(inner, new FirstLocator("=")).map(inner0 -> {
                final var destination = compileValue(inner0.left());
                return generateStatement(depth, destination + " = from");
            });
        });
    }

    private static String compileValue(String value) {
        return compileConstruction(value)
                .or(() -> compileInvocation(value))
                .or(() -> compileDataAccess(value))
                .or(() -> compileFilter(Main::isSymbol, value))
                .or(() -> compileFilter(Main::isNumber, value))
                .or(() -> compileAdd(value))
                .or(() -> compileString(value))
                .orElseGet(() -> invalidate("value", value));
    }

    private static Optional<String> compileString(String value) {
        return truncateLeft(value, "\"").flatMap(inner -> truncateRight(inner, "\"").map(inner0 -> "\"" + inner0 + "\""));
    }

    private static Optional<String> compileAdd(String value) {
        return split(value, new FirstLocator("+")).map(tuple -> {
            return compileValue(tuple.left().strip()) + " + " + compileValue(tuple.right().strip());
        });
    }

    private static boolean isNumber(String input) {
        return IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .allMatch(Character::isDigit);
    }

    private static Optional<String> compileConstruction(String value) {
        return value.startsWith("new ") ? Optional.of(generateInvocation("temp", "")) : Optional.empty();
    }

    private static Optional<String> compileDataAccess(String value) {
        return split(value, new LastLocator(".")).map(tuple -> {
            final var s = compileValue(tuple.left().strip());
            return s + "." + tuple.right().strip();
        });
    }

    private static Optional<String> compileInitialization(String structSegment, int depth) {
        return truncateRight(structSegment, ";").flatMap(inner -> {
            return split(inner, new FirstLocator("=")).flatMap(tuple -> {
                return compileDefinition(tuple.left().strip()).map(definition -> {
                    final var value = compileValue(tuple.right().strip());
                    return generateStatement(depth, definition + " = " + value);
                });
            });
        });
    }

    private static Optional<String> truncateRight(String input, String slice) {
        if (input.endsWith(slice)) return Optional.of(input.substring(0, input.length() - slice.length()));
        return Optional.empty();
    }

    private static Optional<Tuple<String, String>> split(String input, Locator locator) {
        return locator.locate(input).map(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + locator.computeLength());
            return new Tuple<>(left, right);
        });
    }
}
