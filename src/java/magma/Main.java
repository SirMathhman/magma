package magma;

import magma.java.JavaFiles;
import magma.locate.FirstLocator;
import magma.locate.LastLocator;
import magma.locate.Locator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
        return splitAndCompile(root, Main::compileRootSegment);
    }

    private static String splitAndCompile(String root, Function<String, String> compiler) {
        var state = new State();

        final var queue = IntStream.range(0, root.length())
                .mapToObj(root::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            state = splitAtChar(state, c, queue);
        }
        final var segments = state.advance().segments;

        final var output = new StringBuilder();
        for (String segment : segments) {
            final var stripped = segment.strip();
            if (stripped.isEmpty()) continue;
            output.append(compiler.apply(stripped));
        }

        return output.toString();
    }

    private static State splitAtChar(State state, char c, Deque<Character> queue) {
        final var appended = state.append(c);

        if (c == '\'') {
            final var maybeEscape = queue.pop();
            final var withMaybeEscape = appended.append(maybeEscape);
            final var withEscaped = maybeEscape == '\\' ? withMaybeEscape.append(queue.pop()) : withMaybeEscape;
            return withEscaped.append(queue.pop());
        }

        if (c == '"') {
            var current = appended;
            while (!queue.isEmpty()) {
                final var next = queue.pop();
                current = current.append(next);
                if (next == '"') break;
                if (next == '\\') current = current.append(queue.pop());
            }
            return current;
        }

        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{' || c == '(') return appended.enter();
        if (c == '}' || c == ')') return appended.exit();
        return appended;
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
        System.err.println("Invalid " + type + ": " + rootSegment);
        return rootSegment;
    }

    private static Optional<String> compileToStruct(String keyword, String rootSegment) {
        return split(rootSegment, new FirstLocator(keyword)).flatMap(tuple -> {
            return split(tuple.right(), new FirstLocator("{")).flatMap(tuple0 -> {
                return truncateRight(tuple0.right().strip(), "}").map(content -> {
                    final var outputContent = splitAndCompile(content, Main::compileStructSegment);
                    return "struct " + tuple0.left().strip() + " {" + outputContent + "\n};";
                });
            });
        });
    }

    private static String compileStructSegment(String structSegment) {
        return compileInitialization(structSegment)
                .or(() -> compileDefinitionStatement(structSegment))
                .or(() -> compileMethod(structSegment))
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
            final var type = split(left, new LastLocator(" ")).map(Tuple::right).orElse(left);

            return generateDefinition(compileType(type), tuple.right().strip());
        });
    }

    private static String compileType(String type) {
        return compileSymbol(type).orElseGet(() -> invalidate("type", type));
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
        return truncateRight(structSegment, "}")
                .flatMap(inner -> split(inner, new FirstLocator("{"))
                        .flatMap(tuple -> {
                            final var beforeContent = tuple.left();
                            return split(beforeContent, new FirstLocator("(")).flatMap(tuple0 -> {
                                return compileDefinition(tuple0.left().strip()).map(definition -> {
                                    final var inputContent = tuple.right();
                                    final var outputContent = splitAndCompile(inputContent, statement -> compileStatement(statement, 2));
                                    return "\n\t" + definition + "(){" + outputContent + "\n\t}";
                                });
                            });
                        }));
    }

    private static String compileStatement(String statement, int depth) {
        return compileAssignment(statement, depth)
                .or(() -> compileReturn(statement, depth))
                .or(() -> compileInvocation(statement, depth))
                .or(() -> compileIf(statement))
                .orElseGet(() -> invalidate("statement", statement));
    }

    private static Optional<? extends String> compileIf(String statement) {
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
