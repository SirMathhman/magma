package magma.java;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaFiles {
    public static Result<Set<Path>, IOException> walkWrapped(Path path) {
        try (var stream = Files.walk(path)) {
            return new Ok<>(stream.collect(Collectors.toSet()));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }
}
