package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
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
                final var relative = SOURCE_DIRECTORY.relativize(source);
                System.out.println("Compiling source: " + relative);

                final var parent = relative.getParent();
                final var parentDirectory = TARGET_DIRECTORY.resolve(parent);

                if (!Files.exists(parentDirectory)) Files.createDirectories(parentDirectory);

                final var name = source.getFileName().toString();
                final var nameWithoutExt = name.substring(0, name.indexOf('.'));

                final var header = parentDirectory.resolve(nameWithoutExt + ".h");
                Files.writeString(header, "");

                final var target = parentDirectory.resolve(nameWithoutExt + ".c");
                Files.writeString(target, "");
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
