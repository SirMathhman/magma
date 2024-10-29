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

public record JavaPath(Path path) implements Path_ {
    public static final char EXTENSION_SEPARATOR = '.';

    @Override
    public Option<IOException> deleteIfExists() {
        try {
            Files.deleteIfExists(path);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public String_ computeFileNameWithoutExtension() {
        final var name = new JavaString(path.getFileName().toString());
        return name.firstIndexOfChar(EXTENSION_SEPARATOR)
                .flatMap(index -> name.substring(0, index))
                .orElse(name);
    }

    @Override
    public Path_ resolveSibling(String_ siblingName) {
        return new JavaPath(path.resolveSibling(siblingName.unwrap()));
    }

    @Override
    public Option<IOException> writeSafe(String_ content) {
        try {
            Files.writeString(path, content.unwrap());
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }

    @Override
    public Result<String_, IOException> readString() {
        try {
            return new Ok<>(new JavaString(Files.readString(path)));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }
}