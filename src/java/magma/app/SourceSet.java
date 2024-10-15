package magma.app;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public interface SourceSet {
    Set<Path> collect() throws IOException;
}
