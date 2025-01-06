package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        try {
            final var sources = collectSources();
            runWithSources(sources);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static Set<Path> collectSources() throws IOException {
        try (Stream<Path> stream = Files.walk(SOURCE_DIRECTORY)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());
        }
    }

    private static void runWithSources(Set<Path> sources) throws IOException {
        for (Path source : sources) {
            compileSource(source);
        }
    }

    private static void compileSource(Path source) throws IOException {
        final var relative = Main.SOURCE_DIRECTORY.relativize(source);
        final var parent = relative.getParent();
        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var name = relative.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));
        final var target = targetParent.resolve(nameWithoutExt + ".c");
        Files.createFile(target);
    }
}
