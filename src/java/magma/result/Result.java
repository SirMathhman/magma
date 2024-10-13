package magma.result;

import java.util.Optional;
import java.util.function.Function;

public interface Result<T, E> {
    Optional<T> findValue();

    Optional<E> findError();

    <R> Result<R, E> mapValue(Function<T, R> mapper);
}
