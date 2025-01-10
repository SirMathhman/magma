package magma;

import java.nio.file.Path;
import java.util.Set;

public record Ok<T, X>(T value) implements Result<T, X> {
}
