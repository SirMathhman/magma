package magma.app.io.source;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.util.Set;

public interface SourceSet {
    Set<Unit> collect() throws IOException;
}