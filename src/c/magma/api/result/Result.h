package magma.api.result;package magma.api.Tuple;package java.util.Optional;package java.util.function.Function;package java.util.function.Supplier;public interface Result<T, X> {<R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper);<R> Result<R, X> mapValue(Function<T, R> mapper);<R> Result<T, R> mapErr(Function<X, R> mapper);<R> R match(Function<T, R> onOkFunction<T, R> onOk Function<X, R> onErr);<R> Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other);<R> Result<T, Tuple<X, R>> or(Supplier<Result<T, R>> other);Optional<T> findValue();boolean isOk();Optional<X> findError();}