package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
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
        return splitAndCompile(root, Main::compileRootSegment);
    }

    private static Optional<String> splitAndCompile(String input, Function<String, Optional<String>> compiler) {
        final var segments = split(input);

        final var output = new StringBuilder();
        for (String segment : segments) {
            final var optional = compiler.apply(segment.strip());
            if (optional.isEmpty()) return Optional.empty();
            output.append(optional.get());
        }

        return Optional.of(output.toString());
    }

    private static ArrayList<String> split(String input) {
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
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if (c == '}' && depth == 1) {
                depth--;
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }

        advance(buffer, segments);
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
                    final var outputContent = splitAndCompile(content, Main::compileStructSegment);
                    return Optional.of("struct " + name + " {" + outputContent + "\n};");
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
        return Optional.of("\n\ttemp(){\n\t}");
    }

    private static Optional<String> compileInitialization(String structSegment) {
        final var index = structSegment.indexOf("=");
        if (index == -1) return Optional.empty();

        final var stripped = structSegment.substring(0, index).strip();
        final var index1 = stripped.lastIndexOf(' ');
        if (index1 == -1) return Optional.empty();

        final var beforeName = stripped.substring(0, index1).strip();
        final Optional<String> maybeOutputType;
        final var index2 = beforeName.lastIndexOf(' ');
        if (index2 == -1) {
            maybeOutputType = compileType(beforeName);
        } else {
            maybeOutputType = compileType(beforeName.substring(index2 + 1));
        }

        if (maybeOutputType.isEmpty()) return Optional.empty();
        final var outputType = maybeOutputType.get();
        final var name = stripped.substring(index1 + 1).strip();
        return Optional.of("\n\t" + outputType + " " + name + " = 0;");
    }

    private static Optional<String> compileType(String type) {
        if (isSymbol(type)) return Optional.of(type);
        return invalidate("type", type);
    }

    private static boolean isSymbol(String type) {
        for (int i = 0; i < type.length(); i++) {
            final var c = type.charAt(i);
            if (Character.isLetter(c)) continue;
            return false;
        }

        return true;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static Optional<String> invalidate(String type, String input) {
        System.err.println("Invalid " + type + ": " + input);
        return Optional.empty();
    }
}
