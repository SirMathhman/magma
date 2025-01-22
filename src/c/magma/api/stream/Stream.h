import magma.api.result.Result;import java.util.Optional;struct Stream<T>{
	Optional<T> foldLeft(BiFunction<T, T, T> folder);
	<R>R foldLeft(R initial, BiFunction<R, T, R> folder);
	<R>Stream<R> map(Function<T, R> mapper);
	<R, X>Result<R, X> foldLeftToResult(R initial, BiFunction<R, T, Result<R, X>> folder);}