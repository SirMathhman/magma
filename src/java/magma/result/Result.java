package magma.result;

import magma.Tuple;
import magma.option.Option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<T, X> {
    boolean isErr();

    Option<X> findErr();

    Option<T> findValue();

    <R> Result<R, X> preserveErr(R replacement);

    void consume(Consumer<T> onValid, Consumer<X> onError);

    <R> Result<R, X> mapValue(Function<T, R> mapper);

    <R> Result<Tuple<T, R>,X> and(Supplier<Result<R, X>> supplier);

    <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper);
}
