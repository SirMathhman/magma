package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        final var sources = collectSources();
        compileSources(sources);
    }

    private static void compileSources(Set<Path> sources) {
        try {
            for (var source : sources) {
                compileSource(source);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void compileSource(Path source) throws IOException {
        final var relativized = SOURCE_DIRECTORY.relativize(source);
        final var nameWithExt = relativized.getFileName().toString();
        final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

        final var targetParent = TARGET_DIRECTORY.resolve(relativized.getParent());

        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);
        final var target = targetParent.resolve(name + ".mgs");

        final var input = Files.readString(source);
        Files.writeString(target, input);
    }

    private static Set<Path> collectSources() {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            return stream.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".java"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
