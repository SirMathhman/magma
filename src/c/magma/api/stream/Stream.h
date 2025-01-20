import magma.api.result.Result;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
public struct Stream<T> {
	((((T, T) => T)) => Optional<T>) foldLeft;
	<R>((R, ((R, T) => R)) => R) foldLeft;
	<R>((((T) => R)) => Stream<R>) map;
	<R, X>((R, ((R, T) => Result<R, X>)) => Result<R, X>) foldLeftToResult;
}