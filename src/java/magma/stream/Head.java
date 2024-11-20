package magma.stream;

import java.util.Optional;

public interface Head<T> {
    Optional<T> next();
}
