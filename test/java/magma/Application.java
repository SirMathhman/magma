package magma;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public record Application(Path source) {
    public static final char EXTENSION_SEPARATOR = '.';
    public static final String MAGMA_EXTENSION = EXTENSION_SEPARATOR + "mgs";

    private static Option<IOException> writeOutput(Path source, String output) {
        final var fileName = source.getFileName().toString();
        final var nameWithoutExtension = new JavaString(fileName)
                .firstIndexOfChar(EXTENSION_SEPARATOR)
                .map(index -> fileName.substring(0, index))
                .orElse(fileName);

        final var target = source.resolveSibling(nameWithoutExtension + MAGMA_EXTENSION);
        return writeSafe(target, output);
    }

    private static String compile(String input) {
        if (input.equals(renderImport())) {
            return input;
        } else {
            return "";
        }
    }

    static String renderImport() {
        return "import magma;";
    }

    static Option<IOException> writeSafe(Path path, String content) {
        try {
            Files.writeString(path, content);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }

    static Result<String, IOException> readSafe(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    Option<IOException> run() {
        if (!Files.exists(source())) return new None<>();

        return readSafe(source())
                .mapValue(Application::compile)
                .mapValue(output -> writeOutput(source(), output))
                .match(Function.identity(), Some::new);
    }
}