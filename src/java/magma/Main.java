package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (var stream = Files.walk(sourceDirectory)) {
            final var files = stream.filter(file -> file.getFileName().toString().endsWith(".java"))
                    .toList();

            for (Path file : files) {
                final var input = Files.readString(file);
                final var relativized = sourceDirectory.relativize(file);
                final var parent = relativized.getParent();
                final var targetDirectory = Paths.get(".", "src", "magma");
                final var targetParent = targetDirectory.resolve(parent);
                if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

                final var fileName = file.getFileName().toString();
                final var separator = fileName.indexOf('.');
                var name = fileName.substring(0, separator);
                final var target = targetParent.resolve(name + ".mgs");
                Files.writeString(target, compileRoot(input));
            }
        } catch (IOException | CompileException e) {
            throw new RuntimeException(e);
        }
    }

    private static String compileRoot(String root) throws CompileException {
        throw new CompileException("Invalid root", root);
    }
}
