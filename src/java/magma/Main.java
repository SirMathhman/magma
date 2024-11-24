package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        final java.util.Set<Path> sources;
        try (var stream = Files.walk(sourceDirectory)) {
            sources = stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".java"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            final var targetDirectory = Paths.get(".", "src", "magma");
            for (var source : sources) {
                final var relativized = sourceDirectory.relativize(source);
                final var nameWithExt = relativized.getFileName().toString();
                final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

                final var targetParent = targetDirectory.resolve(relativized.getParent());

                if (!Files.exists(targetParent)) Files.createDirectories(targetParent);
                final var target = targetParent.resolve(name + ".mgs");

                final var input = Files.readString(source);
                Files.writeString(target, input);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
