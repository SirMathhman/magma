package magma.app;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public record SingleSourceSet(Path source) implements SourceSet {
    private Set<Path> collect0() {
        var sources = new HashSet<Path>();
        if(Files.exists(source())) sources.add(source());
        return sources;
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