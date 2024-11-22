package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        throw new CompileException("Unknown input", input);
    }

    private static List<String> findNamespace(Path path) {
        return IntStream.range(0, path.getNameCount() - 1)
                .mapToObj(path::getName)
                .map(Path::toString)
                .toList();
    }
}
