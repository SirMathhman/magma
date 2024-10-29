package magma.java;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record JavaPath(Path path) {
    public static final char EXTENSION_SEPARATOR = '.';

    public String computeFileNameWithoutExtension() {
        final var fileName = path().getFileName().toString();

        return new JavaString(fileName)
                .firstIndexOfChar(EXTENSION_SEPARATOR)
                .map(index -> fileName.substring(0, index))
                .orElse(fileName);
    }

    public JavaPath resolveSibling(String siblingName) {
        return new JavaPath(path().resolveSibling(siblingName));
    }

    public Option<IOException> writeSafe(String content) {
        try {
            Files.writeString(path(), content);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }

    public Result<String, IOException> readString() {
        try {
            return new Ok<>(Files.readString(path()));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    public boolean isExists() {
        return Files.exists(path());
    }
}