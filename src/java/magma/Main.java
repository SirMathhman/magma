package magma;

import magma.java.JavaFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            state = splitAtChar(state, c);
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

    private static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    private static String compileRootSegment(String rootSegment) {
        if (rootSegment.startsWith("package")) return "";
        if (rootSegment.startsWith("import")) return "#include \"temp.h\"\n";

        return compileToStruct("class", rootSegment)
                .or(() -> compileToStruct("record", rootSegment))
                .orElseGet(() -> invalidate("root segment", rootSegment));
    }

    private static String invalidate(String type, String rootSegment) {
        System.err.println("Invalid " + type + ": " + rootSegment);
        return rootSegment;
    }

    private static Optional<String> compileToStruct(String keyword, String rootSegment) {
        return split(rootSegment, keyword).flatMap(tuple -> {
            return split(tuple.right(), "{").flatMap(tuple0 -> {
                return truncateRight(tuple0.right().strip(), "}").map(content -> {
                    final var outputContent = splitAndCompile(content, Main::compileStructSegment);
                    return "struct " + tuple0.left().strip() + " {" + outputContent + "\n};";
                });
            });
        });
    }

    private static String compileStructSegment(String structSegment) {
        return compileInitialization(structSegment)
                .or(() -> compileDefinition(structSegment))
                .or(() -> compileMethod(structSegment))
                .orElseGet(() -> invalidate("struct segment", structSegment));
    }

    private static Optional<String> compileDefinition(String structSegment) {
        return truncateRight(structSegment, ";").map(inner -> generateStatement(generateDefinition()));
    }

    private static String generateDefinition() {
        return "int value";
    }

    private static String generateStatement(String content) {
        return "\n\t" + content + ";";
    }

    private static Optional<String> compileMethod(String structSegment) {
        return truncateRight(structSegment, "}")
                .flatMap(inner -> split(inner, "{")
                        .map(tuple -> {
                            final var inputContent = tuple.right();
                            final var outputContent = splitAndCompile(inputContent, Main::compileStatement);
                            return "\n\tvoid temp(){" + outputContent + "\n\t}";
                        }));
    }

    private static String compileStatement(String statement) {
        return compileAssignment(statement)
                .or(() -> compileReturn(statement))
                .or(() -> compileInvocation(statement))
                .orElseGet(() -> invalidate("statement", statement));
    }

    private static Optional<String> compileInvocation(String statement) {
        return split(statement.strip(), "(").flatMap(inner -> truncateRight(inner.right(), ")").map(inner0 -> {
            return generateStatement("temp()");
        }));
    }

    private static Optional<String> compileReturn(String statement) {
        return truncateLeft(statement, "return").flatMap(inner -> truncateRight(inner, ";").map(inner0 -> {
            return generateStatement("return temp");
        }));
    }

    private static Optional<String> truncateLeft(String input, String slice) {
        return input.startsWith(slice) ? Optional.of(input.substring(slice.length())) : Optional.empty();
    }

    private static Optional<String> compileAssignment(String statement) {
        return split(statement, "=").map(inner -> generateStatement("to = from"));
    }

    private static Optional<String> compileInitialization(String structSegment) {
        return truncateRight(structSegment, ";").flatMap(inner -> {
            return split(inner, "=").map(value -> generateStatement(generateDefinition() + " = 0"));
        });
    }

    private static Optional<String> truncateRight(String input, String slice) {
        if (input.endsWith(slice)) return Optional.of(input.substring(0, input.length() - slice.length()));
        return Optional.empty();
    }

    private static Optional<Tuple<String, String>> split(String input, String slice) {
        final var index = input.indexOf(slice);
        if (index == -1) return Optional.empty();

        final var left = input.substring(0, index);
        final var right = input.substring(index + slice.length());
        return Optional.of(new Tuple<>(left, right));
    }
}
