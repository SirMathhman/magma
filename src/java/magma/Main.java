package magma;

import magma.java.JavaFiles;
import magma.locate.FirstLocator;
import magma.locate.LastLocator;
import magma.locate.Locator;
import magma.locate.TypeLocator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
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
        final var namespace = new ArrayList<String>();
        for (int i = 0; i < parent.getNameCount(); i++) {
            namespace.add(parent.getName(i).toString());
        }

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

    private static String compileRoot(String root) {
        return compileAndMerge(slicesOf(Main::statementChars, root), Main::compileRootSegment, StringBuilder::append);
    }

    private static String compileAndMerge(
            List<String> segments, Function<String, String> compiler, BiFunction<StringBuilder, String, StringBuilder> merger
    ) {
        var output = new StringBuilder();
        for (String segment : segments) {
            final var stripped = segment.strip();
            if (stripped.isEmpty()) continue;
            final var compiled = compiler.apply(stripped);
            output = merger.apply(output, compiled);
        }

        return output.toString();
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
        return writeError("Invalid depth '" + state.depth + "'", root, segments);
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
        return writeError("Invalid " + type, rootSegment, rootSegment);
    }

    private static <T> T writeError(String message, String rootSegment, T value) {
        System.err.println(message + ": " + rootSegment);
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
        return compileInitialization(structSegment)
                .or(() -> compileMethod(structSegment))
                .or(() -> compileDefinitionStatement(structSegment))
                .orElseGet(() -> invalidate("struct segment", structSegment));
    }

    private static Optional<String> compileDefinitionStatement(String structSegment) {
        return truncateRight(structSegment, ";").flatMap(inner -> {
            return compileDefinition(inner).map(inner0 -> generateStatement(1, inner0));
        });
    }

    private static Optional<String> compileDefinition(String definition) {
        return split(definition, new LastLocator(" ")).map(tuple -> {
            final var left = tuple.left().strip();
            final var type = split(left, new TypeLocator()).map(Tuple::right).orElse(left);

            return generateDefinition(compileType(type), tuple.right().strip());
        });
    }

    private static String compileType(String type) {
        return compileSymbol(type)
                .or(() -> compileGeneric(type))
                .or(() -> compileArray(type))
                .orElseGet(() -> invalidate("type", type));
    }

    private static Optional<String> compileArray(String type) {
        return truncateRight(type, "[]").map(inner -> compileType(inner) + "[]");
    }

    private static Optional<String> compileGeneric(String type) {
        return truncateRight(type, ">").flatMap(inner -> split(inner, new FirstLocator("<")).map(tuple -> {
            final var caller = tuple.left();
            final var compiledArgs = compileAndMerge(slicesOf(Main::valueStrings, tuple.right()), Main::compileType, Main::mergeValues);
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

    private static Optional<String> compileSymbol(String type) {
        for (int i = 0; i < type.length(); i++) {
            final var c = type.charAt(i);
            if (Character.isLetter(c)) continue;
            return Optional.empty();
        }

        return Optional.of(type);
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
                final var compiledParams = compileAndMerge(slicesOf(Main::valueStrings, tuple1.right()),
                        segment -> compileDefinition(segment).orElseGet(() -> invalidate("definition", segment)),
                        Main::mergeValues);

                return compileDefinition(inputDefinition).map(definition -> {
                    final var outputContent = truncateLeft(maybeContent, "{").flatMap(inner -> truncateRight(inner, "}").map(inner0 -> {
                        return "{" + compileAndMerge(slicesOf(Main::statementChars, inner0), statement -> compileStatement(statement, 2), StringBuilder::append) + "\n\t}";
                    })).orElse(";");

                    return "\n\t" + definition + "(" + compiledParams + ")" + outputContent;
                });
            });
        });
    }

    private static String compileStatement(String statement, int depth) {
        return compileAssignment(statement, depth)
                .or(() -> compileReturn(statement, depth))
                .or(() -> compileInvocation(statement, depth))
                .or(() -> compileIf(statement))
                .orElseGet(() -> invalidate("statement", statement));
    }

    private static Optional<String> compileIf(String statement) {
        return truncateLeft(statement, "if").map(inner -> "if (1) {}");
    }

    private static Optional<String> compileInvocation(String statement, int depth) {
        return split(statement.strip(), new FirstLocator("(")).flatMap(inner -> truncateRight(inner.right(), ");").map(inner0 -> {
            return generateStatement(depth, "temp()");
        }));
    }

    private static Optional<String> compileReturn(String statement, int depth) {
        return truncateLeft(statement, "return").flatMap(inner -> truncateRight(inner, ";").map(inner0 -> {
            return generateStatement(depth, "return temp");
        }));
    }

    private static Optional<String> truncateLeft(String input, String slice) {
        return input.startsWith(slice) ? Optional.of(input.substring(slice.length())) : Optional.empty();
    }

    private static Optional<String> compileAssignment(String statement, int depth) {
        return split(statement, new FirstLocator("=")).map(inner -> generateStatement(depth, "to = from"));
    }

    private static Optional<String> compileInitialization(String structSegment) {
        return truncateRight(structSegment, ";").flatMap(inner -> {
            return split(inner, new FirstLocator("=")).map(value -> generateStatement(1, generateDefinition("int", "value") + " = 0"));
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
