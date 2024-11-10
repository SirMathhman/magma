package magma.api.result;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.app.compile.MapNode;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<T, E> {
    <R> Result<R, E> mapValue(Function<T, R> mapper);

    <R> R match(Function<T, R> onValid, Function<E, R> onError);

    Option<T> findValue();

    <R> Result<R, E> flatMapValue(Function<T, Result<R, E>> mapper);

    <R> Result<T, R> mapErr(Function<E, R> mapper);

    <R> Result<Tuple<T, R>, E> and(Supplier<Result<R, E>> supplier);

    boolean isErr();

    Option<E> findErr();

    boolean isOk();

    void consume(Consumer<T> withValue, Consumer<E> withError);
}
