package magma.java;

import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class JavaFiles {
    public static Result<List<Path>, IOException> walk(Path directory) {
        try (final var stream = Files.walk(directory)) {
            return new Ok<>(stream.toList());
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    public static Optional<IOException> createDirectories(Path targetParent) {
        try {
            Files.createDirectories(targetParent);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    public static Optional<IOException> writeSafe(Path target, String output) {
        try {
            Files.writeString(target, output);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    public static Result<String, IOException> readSafe(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }
}
