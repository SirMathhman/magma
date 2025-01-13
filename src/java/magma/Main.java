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
        try (final var stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                final var relativized = SOURCE_DIRECTORY.relativize(source);
                final var parent = relativized.getParent();
                final var name = relativized.getFileName().toString();
                final var nameWithoutExt = name.substring(0, name.indexOf('.'));
                final var targetParent = TARGET_DIRECTORY.resolve(parent);
                if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

                final var target = targetParent.resolve(nameWithoutExt + ".c");
                final var input = Files.readString(source);
                Files.writeString(target, compileRoot(input));
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compileRoot(String root) {
        System.err.println("Invalid root: " + root);
        return root;
    }
}
