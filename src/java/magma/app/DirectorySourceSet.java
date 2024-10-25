package magma.app;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public final class DirectorySourceSet implements SourceSet {
    private final Path root;
    private final String extension;

    public DirectorySourceSet(Path root, String extension) {
        this.root = root;
        this.extension = extension;
    }

    @Override
    public Result<Set<PathSource>, IOException> collect() {
        try {
            var stream = Files.walk(root);
            return new Ok<>(stream
                    .filter(path -> path.toString().endsWith(extension))
                    .map(path -> new PathSource(root, path))
                    .collect(Collectors.toSet()));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }
}
