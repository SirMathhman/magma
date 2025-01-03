package magma;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public record SingleSourceSet(Path source) implements SourceSet {
    @Override
    public Set<Source> collect() {
        if (Files.exists(source())) {
            return Collections.singleton(new PathSource(source));
        } else {
            return Collections.emptySet();
        }
    }
}