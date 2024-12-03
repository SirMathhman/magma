package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
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
                final var fileName = source.getFileName().toString();
                final var separator = fileName.indexOf('.');
                final var name = fileName.substring(0, separator);

                final var input = Files.readString(source);
                Files.writeString(source.resolveSibling(name + ".mgs"), input);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
