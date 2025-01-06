package java;

import magma.Err;
import magma.Ok;
import magma.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
}
