package magma.app;

import magma.api.result.Ok;
import magma.api.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public record SingleSourceSet(Path source) implements SourceSet {

    @Override
    public Result<Set<Path>, IOException> collect() {
        var sources = new HashSet<Path>();
        if(Files.exists(this.source())) sources.add(this.source());
        return new Ok<>(sources);
    }
}