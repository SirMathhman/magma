package magma.api.result;

import magma.api.Tuple;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<T, E> {
    Optional<T> findValue();

    Optional<E> findError();

    <R> Result<R, E> mapValue(Function<T, R> mapper);

    boolean isErr();

    <R> Result<Tuple<T, R>, E> and(Supplier<Result<R, E>> supplier);

    boolean isOk();

    <R> Result<R, E> flatMapValue(Function<T, Result<R, E>> mapper);

    <R> Result<T, R> mapErr(Function<E, R> mapper);

    <R> R match(Function<T, R> onValid, Function<E, R> onError);
}
