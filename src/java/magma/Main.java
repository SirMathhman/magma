package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                runWithSource(source);
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void runWithSource(Path source) throws IOException {
        final var relativized = SOURCE_DIRECTORY.relativize(source);
        final var relativeParent = relativized.getParent();
        final var relativeName = relativized.getFileName().toString();
        final var separator = relativeName.indexOf('.');
        final var name = relativeName.substring(0, separator);

        final var targetParent = TARGET_DIRECTORY.resolve(relativeParent);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var target = targetParent.resolve(name + ".c");
        Files.createFile(target);
    }
}
