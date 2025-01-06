package jv;

import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaFiles {
    public static Result<Set<Path>, IOException> walkSafe(Path path) {
        try (var paths = Files.walk(path)) {
            return new Ok<>(paths.collect(Collectors.toSet()));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    public static Optional<IOException> writeSafe(String output, Path target) {
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

    public static Optional<IOException> createDirectoriesSafe(Path targetParent) {
        try {
            Files.createDirectories(targetParent);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }
}
