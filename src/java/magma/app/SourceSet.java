package magma.app;

import magma.api.result.Result;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public interface SourceSet {
    Result<Set<PathSource>, IOException> collect();
}
