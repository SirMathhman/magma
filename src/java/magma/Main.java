package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        try (Stream<Path> stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                compileSource(source);
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void compileSource(Path source) throws IOException {
        final var relativized = SOURCE_DIRECTORY.relativize(source);
        final var parent = relativized.getParent();
        final var nameWithExt = relativized.getFileName().toString();
        final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var input = Files.readString(source);
        final var output = compile(input).orElse("");

        final var target = targetParent.resolve(name + ".c");
        Files.writeString(target, output);

        final var header = targetParent.resolve(name + ".h");
        Files.writeString(header, output);
    }

    private static Optional<String> compile(String root) {
        return compileAll(Main.split(root), Main::compileRootSegment);
    }

    private static Optional<String> compileAll(List<String> segments, Function<String, Optional<String>> compiler) {
        final var output = new StringBuilder();
        for (String segment : segments) {
            final var stripped = segment.strip();
            if (stripped.isEmpty()) continue;

            final var optional = compiler.apply(stripped);
            if (optional.isEmpty()) return Optional.empty();
            output.append(optional.get());
        }

        return Optional.of(output.toString());
    }

    private static List<String> split(String input) {
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

                if (c1 == '\\') buffer.append(queue.pop());
                buffer.append(queue.pop());
            }

            if (c == '"') {
                while (!queue.isEmpty()) {
                    final var c1 = queue.pop();
                    buffer.append(c1);

                    if (c1 == '"') break;
                    if (c1 == '\\') buffer.append(queue.pop());
                }
            }

            if (c == ';' && depth == 0) {
                advance(segments, buffer);
                buffer = new StringBuilder();
            } else if (c == '}' && depth == 1) {
                depth--;
                advance(segments, buffer);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }

        advance(segments, buffer);
        if (depth != 0) {
            System.err.println("Invalid depth: '" + depth + "': " + input);
        }

        return segments;
    }

    private static Optional<String> compileRootSegment(String rootSegment) {
        if (rootSegment.startsWith("package ")) return Optional.of("");
        if (rootSegment.startsWith("import ")) return Optional.of("#include \"temp.h\"\n");
        final var keyword = rootSegment.indexOf("class");
        if (keyword != -1) {
            final var afterKeyword = rootSegment.substring(keyword + "class".length());
            final var contentStart = afterKeyword.indexOf('{');
            if (contentStart != -1) {
                final var name = afterKeyword.substring(0, contentStart).strip();
                final var withEnd = afterKeyword.substring(contentStart + 1).strip();
                if (withEnd.endsWith("}")) {
                    final var content = withEnd.substring(0, withEnd.length() - 1);
                    final var maybeOutputContent = compileAll(Main.split(content), Main::compileStructSegment);
                    if (maybeOutputContent.isPresent()) {
                        return Optional.of("struct " + name + " {" + maybeOutputContent.get() + "\n};");
                    }
                }
            }
        }
        return invalidate("root segment", rootSegment);
    }

    private static Optional<String> compileStructSegment(String structSegment) {
        return compileInitialization(structSegment)
                .or(() -> compileMethod(structSegment))
                .or(() -> invalidate("struct segment", structSegment));
    }

    private static Optional<? extends String> compileMethod(String structSegment) {
        final var index = structSegment.indexOf('(');
        if (index != -1) {
            final var inputDefinition = structSegment.substring(0, index).strip();
            return compileDefinition(inputDefinition).map(outputDefinition -> "\n\t" + outputDefinition + "(){\n\t}");
        }

        return Optional.empty();
    }

    private static Optional<String> compileInitialization(String structSegment) {
        final var index = structSegment.indexOf("=");
        if (index == -1) return Optional.empty();

        final var definition = structSegment.substring(0, index).strip();
        return compileDefinition(definition).map(outputDefinition -> "\n\t" + outputDefinition + " = 0;");
    }

    private static Optional<String> compileDefinition(String definition) {
        final var index1 = definition.lastIndexOf(' ');
        if (index1 == -1) return Optional.empty();

        final var beforeName = definition.substring(0, index1).strip();
        final Optional<String> maybeOutputType;
        final var index2 = beforeName.lastIndexOf(' ');
        if (index2 == -1) {
            maybeOutputType = compileType(beforeName);
        } else {
            maybeOutputType = compileType(beforeName.substring(index2 + 1));
        }

        if (maybeOutputType.isEmpty()) return Optional.empty();
        final var outputType = maybeOutputType.get();
        final var name = definition.substring(index1 + 1).strip();
        return Optional.of(outputType + " " + name);
    }

    private static Optional<String> compileType(String type) {
        if (isSymbol(type)) return Optional.of(type);

        return compileGeneric(type).or(() -> invalidate("type", type));
    }

    private static Optional<String> compileGeneric(String type) {
        final var index = type.indexOf('<');
        if (index == -1) return Optional.empty();

        final var caller = type.substring(0, index).strip();
        final var stripped = type.substring(index + 1).strip();
        if (!stripped.endsWith(">")) return Optional.empty();

        final var substring = stripped.substring(0, stripped.length() - 1);
        return compileAll(splitValues(substring), Main::compileType).map(compiled -> caller + "<" + compiled + ">");
    }

    private static List<String> splitValues(String input) {
        final var segments = new ArrayList<String>();
        final var buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            final var c = input.charAt(i);
            if (c == ',') {
                advance(segments, buffer);
            } else {
                buffer.append(c);
            }
        }

        advance(segments, buffer);
        return segments;
    }

    private static boolean isSymbol(String type) {
        for (int i = 0; i < type.length(); i++) {
            final var c = type.charAt(i);
            if (Character.isLetter(c)) continue;
            return false;
        }

        return true;
    }

    private static void advance(ArrayList<String> segments, StringBuilder buffer) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static Optional<String> invalidate(String type, String input) {
        System.err.println("Invalid " + type + ": " + input);
        return Optional.empty();
    }
}
