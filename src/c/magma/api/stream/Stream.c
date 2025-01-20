import magma.api.result.Result;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
public struct Stream<T> {
	(([Capture, ((Capture, T, T) => T)]) => Optional<T>) foldLeft;
	<R>((R, [Capture, ((Capture, R, T) => R)]) => R) foldLeft;
	<R>(([Capture, ((Capture, T) => R)]) => Stream<R>) map;
	<R, X>((R, [Capture, ((Capture, R, T) => Result<R, X>)]) => Result<R, X>) foldLeftToResult;
}