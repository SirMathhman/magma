package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record Application(SourceSet sourceSet) {
    private static void runWithSource(Path source) throws IOException, CompileException {
        final var input = Files.readString(source);
        final var output = compile(input);

        final var name = source.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));
        final var target = source.resolveSibling(nameWithoutExt + ".c");
        Files.writeString(target, output);
    }

    private static String compile(String input) throws CompileException {
        if (input.isEmpty()) {
            return "";
        } else {
            throw new CompileException("Unknown input", input);
        }
    }

    void run() throws IOException, CompileException {
        final var sources = sourceSet().collect();

        for (Path source : sources) {
            runWithSource(source);
        }
    }
}