package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (var stream = Files.walk(sourceDirectory)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .toList();

            for (Path source : sources) {
                final var relativeSourceParent = sourceDirectory.relativize(source.getParent());
                final var targetDirectory = Paths.get(".", "src", "magma");
                final var targetParent = targetDirectory.resolve(relativeSourceParent);
                if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

                final var name = source.getFileName().toString();
                final var separator = name.indexOf('.');
                final var nameWithoutExt = name.substring(0, separator);
                final var target = targetParent.resolve(nameWithoutExt + ".mgs");
                Files.writeString(target, Files.readString(source));
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
