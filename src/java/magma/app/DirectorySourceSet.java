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
    private Set<Path> collect0() throws IOException {
        try (var stream = Files.walk(root)) {
            return stream
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public Result<Set<Path>, IOException> collect() {
        try {
            return new Ok<>(collect0());
        } catch (IOException e) {
            return new Err<>(e);
        }
    }
}
