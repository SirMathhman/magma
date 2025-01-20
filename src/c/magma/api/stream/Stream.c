import magma.api.result.Result;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
public struct Stream<T> {
	Optional<T> foldLeft(((T, T) => T) folder);
	<R>R foldLeft(R initial, ((R, T) => R) folder);
	<R>Stream<R> map(((T) => R) mapper);
	<R, X>Result<R, X> foldLeftToResult(R initial, ((R, T) => Result<R, X>) folder);
}