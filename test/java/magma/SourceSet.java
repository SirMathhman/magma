package magma;

import java.nio.file.Path;
import java.util.Set;

public interface SourceSet {
    Set<Path> collect();
}
