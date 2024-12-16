package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .toList();

            for (Path source : sources) {
                runWithSource(source);
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void runWithSource(Path source) throws IOException {
        final var relativeSourceParent = Main.SOURCE_DIRECTORY.relativize(source.getParent());
        final var targetParent = TARGET_DIRECTORY.resolve(relativeSourceParent);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var name = source.getFileName().toString();
        final var separator = name.indexOf('.');
        final var nameWithoutExt = name.substring(0, separator);
        final var target = targetParent.resolve(nameWithoutExt + ".mgs");
        Files.writeString(target, Files.readString(source));
    }
}
