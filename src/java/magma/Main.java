package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (Stream<Path> stream = Files.walk(sourceDirectory)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .toList();

            runWithSources(sources, sourceDirectory);
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void runWithSources(List<Path> sources, Path sourceDirectory) throws IOException, CompileException {
        for (Path source : sources) {
            runWithSource(sourceDirectory, source);
        }
    }

    private static void runWithSource(Path sourceDirectory, Path source) throws IOException, CompileException {
        final var relativized = sourceDirectory.relativize(source);
        final var parent = relativized.getParent();
        final var targetDirectory = Paths.get(".", "src", "c");
        final var targetParent = targetDirectory.resolve(parent);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var name = relativized.getFileName().toString();
        final var separator = name.indexOf('.');
        final var nameWithoutExt = name.substring(0, separator);

        final var input = Files.readString(source);
        compileRoot(input);

        Files.createFile(targetParent.resolve(nameWithoutExt + ".c"));
    }

    private static String compileRoot(String root) throws CompileException {
        throw new CompileException("Unknown root", root);
    }
}
