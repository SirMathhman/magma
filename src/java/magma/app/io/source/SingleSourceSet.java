package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public record SingleSourceSet(Path source) implements SourceSet {
    @Override
    public Set<Unit> collect() {
        if (Files.exists(source())) {
            return Collections.singleton(new PathUnit(source));
        } else {
            return Collections.emptySet();
        }
    }
}