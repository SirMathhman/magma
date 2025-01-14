package magma;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<T, X> {
    <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper);

    <R> Result<R, X> mapValue(Function<T, R> mapper);

    boolean isValid();

    <R> Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other);

    Optional<T> findValue();

    Optional<X> findError();
}
