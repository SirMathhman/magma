package magma.app;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public record DirectorySourceSet(Path root) implements SourceSet {

    @Override
    public Result<Set<Path>, IOException> collect() {
        try (var stream = Files.walk(root)) {
            return new Ok<>(stream
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet()));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }
}
