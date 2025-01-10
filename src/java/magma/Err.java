package magma;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public record Err<T, X>(X error) implements Result<T, X> {
}
