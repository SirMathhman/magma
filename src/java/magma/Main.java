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
                    .filter(file -> file.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                final var sourceParent = sourceDirectory.relativize(source.getParent());
                final var targetDirectory = Paths.get(".", "src", "magma");
                final var targetParent = targetDirectory.resolve(sourceParent);
                if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

                final var name = source.getFileName().toString();
                final var slice = name.substring(0, name.indexOf('.'));

                final var target = targetParent.resolve(slice + ".mgs");
                Files.writeString(target, Files.readString(source));
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
