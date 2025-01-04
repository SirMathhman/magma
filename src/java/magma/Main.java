package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (var stream = Files.walk(sourceDirectory)) {
            final var sources = stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                runWithSource(source, sourceDirectory);
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void runWithSource(Path source, Path sourceDirectory) throws IOException {
        final var relativized = sourceDirectory.relativize(source);
        final var name = relativized.getFileName().toString();
        final var index = name.indexOf('.');
        if(index == -1) throw new RuntimeException("Invalid file name: " + relativized);

        final var nameWithoutExt = name.substring(0, index);

        final var targetParent = Paths.get(".", "src", "c").resolve(relativized.getParent());
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var target = targetParent.resolve(nameWithoutExt + ".c");
        Files.writeString(target, "");
    }
}
