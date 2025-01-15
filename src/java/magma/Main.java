package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (Stream<Path> stream = Files.walk(sourceDirectory)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                final var relativized = sourceDirectory.relativize(source);
                final var parent = relativized.getParent();

                final var targetDirectory = Paths.get(".", "src", "c");
                final var targetParent = targetDirectory.resolve(parent);
                if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

                final var name = relativized.getFileName().toString();
                final var nameWithoutExt = name.substring(0, name.indexOf('.'));

                final var header = targetParent.resolve(nameWithoutExt + ".h");
                Files.writeString(header, "");

                final var target = targetParent.resolve(nameWithoutExt + ".c");
                Files.writeString(target, "");
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
