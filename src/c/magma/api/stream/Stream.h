package magma.api.stream;package magma.api.result.Result;package java.util.Optional;package java.util.function.BiFunction;package java.util.function.Function;public interface Stream<T> {Optional<T> foldLeft(BiFunction<T, T, T> folder);<R> R foldLeft(R initialR initial BiFunction<R, T, R> folder);<R> Stream<R> map(Function<T, R> mapper);<R, X> Result<R, X> foldLeftToResult(R initialR initial BiFunction<R, T, Result<R, X>> folder);}