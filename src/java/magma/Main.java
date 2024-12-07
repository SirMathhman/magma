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
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        try (Stream<Path> stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            runWithSources(sources);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void runWithSources(Set<Path> sources) throws IOException {
        for (Path source : sources) {
            final var sourceParent = Main.SOURCE_DIRECTORY.relativize(source.getParent());
            final var targetParent = TARGET_DIRECTORY.resolve(sourceParent);
            if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

            final var name = source.getFileName().toString();
            final var slice = name.substring(0, name.indexOf('.'));

            final var target = targetParent.resolve(slice + ".mgs");
            Files.writeString(target, Files.readString(source));
        }
    }
}
