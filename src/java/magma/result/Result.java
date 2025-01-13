package magma.result;

import magma.Tuple;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<T, X> {
    <R> Result<R, X> mapValue(Function<T, R> mapper);

    <R> R match(Function<T, R> onOk, Function<X, R> onErr);

    <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper);

    Optional<T> findValue();

    boolean isOk();

    <R> Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other);

    <R> Result<T, R> mapErr(Function<X, R> mapper);

    Optional<X> findError();
}
