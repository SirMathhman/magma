package magma.result;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<T, E> {
    Optional<T> findValue();

    <R> Result<Tuple<T, R>, E> and(Supplier<Result<R, E>> other);

    <R> Result<R, E> mapValue(Function<T, R> mapper);

    <R> Result<R, E> flatMapValue(Function<T, Result<R, E>> mapper);

    Optional<E> findError();
}
