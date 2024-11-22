package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            compileSources(stream);
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void compileSources(Stream<Path> stream) throws IOException, CompileException {
        final var files = stream
                .filter(file -> file.getFileName().toString().endsWith(".java"))
                .collect(Collectors.toSet());

        for (Path sourceFile : files) {
            compileSource(sourceFile);
        }
    }

    private static void compileSource(Path source) throws IOException, CompileException {
        final var sourceRelativized = SOURCE_DIRECTORY.relativize(source);
        final var sourceNamespace = findNamespace(sourceRelativized);

        final var targetParent = sourceNamespace.stream().reduce(TARGET_DIRECTORY, Path::resolve, (_, next) -> next);

        final var nameWithExtension = source.getFileName().toString();
        final var name = nameWithExtension.substring(0, nameWithExtension.indexOf('.'));

        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);
        final var target = targetParent.resolve(name + ".mgs");

        final var input = Files.readString(source);
        Files.writeString(target, compile(input));
    }

    private static String compile(String input) throws CompileException {
        final var segments = split(input);
        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootMember(segment));
        }

        return output.toString();
    }

    private static List<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            buffer.append(c);
            if (c == ';') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static String compileRootMember(String input) throws CompileException {
        throw new CompileException("Unknown input", input);
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static List<String> findNamespace(Path path) {
        return IntStream.range(0, path.getNameCount() - 1)
                .mapToObj(path::getName)
                .map(Path::toString)
                .toList();
    }
}
