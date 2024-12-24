package magma;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<T, X> {
    Optional<T> findValue();

    Optional<X> findError();

    <R> Result<R, X> mapValue(Function<T, R> mapper);

    <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper);

    <R> Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other);

    boolean isOk();

    <R> Result<T, R> mapErr(Function<X, R> mapper);

    <R> R match(Function<T, R> onOk, Function<X, R> onErr);
}
