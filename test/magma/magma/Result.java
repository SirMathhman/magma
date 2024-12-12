package magma;

import java.util.Optional;

public interface Result<T, X> {
    Optional<T> findValue();

    boolean isErr();
}
