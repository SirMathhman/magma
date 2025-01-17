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
                final var relativized = SOURCE_DIRECTORY.relativize(source);
                final var parent = relativized.getParent();
                final var nameWithExt = relativized.getFileName().toString();
                final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

                final var targetParent = TARGET_DIRECTORY.resolve(parent);
                if (!Files.exists(targetParent)) Files.createDirectories(targetParent);
                
                final var output = Files.readString(source);

                final var target = targetParent.resolve(name + ".c");
                Files.writeString(target, output);

                final var header = targetParent.resolve(name + ".h");
                Files.writeString(header, output);
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
