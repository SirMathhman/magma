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

    private String computeFileNameWithoutExtension1() {
        final var fileName = path().getFileName().toString();

        return new JavaString(fileName)
                .firstIndexOfChar(EXTENSION_SEPARATOR)
                .map(index -> fileName.substring(0, index))
                .orElse(fileName);
    }

    private Path_ resolveSibling0(String siblingName) {
        return new JavaPath(path().resolveSibling(siblingName));
    }

    private Option<IOException> writeSafe0(String content) {
        try {
            Files.writeString(path(), content);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }

    private Result<String, IOException> readString0() {
        try {
            return new Ok<>(Files.readString(path()));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    @Override
    public boolean exists() {
        return Files.exists(path());
    }

    @Override
    public String_ computeFileNameWithoutExtension() {
        return new JavaString(computeFileNameWithoutExtension1());
    }

    @Override
    public Path_ resolveSibling(String_ siblingName) {
        return resolveSibling0(siblingName.unwrap());
    }

    @Override
    public Option<IOException> writeSafe(String_ content) {
        return writeSafe0(content.unwrap());
    }

    @Override
    public Result<String_, IOException> readString() {
        return readString0().mapValue(JavaString::new);
    }
}