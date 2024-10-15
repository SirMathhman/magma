package magma.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public record SingleSourceSet(Path source) implements SourceSet {
    @Override
    public Set<Path> collect() {
        var sources = new HashSet<Path>();
        if(Files.exists(source())) sources.add(source());
        return sources;
    }
}