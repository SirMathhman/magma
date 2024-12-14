package magma;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<T, X> {
    <R> Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other);

    <R> Result<R, X> mapValue(Function<T, R> mapper);

    <R> R match(Function<T, R> onOk, Function<X, R> onErr);

    <R> Result<T, R> mapErr(Function<X, R> mapper);
}
