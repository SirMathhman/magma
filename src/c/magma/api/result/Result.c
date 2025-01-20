import magma.api.Tuple;
import magma.api.option.Option;
import java.util.function.Function;
import java.util.function.Supplier;
public struct Result<T, X> {
	<R>((((T) => Result<R, X>)) => Result<R, X>) flatMapValue;
	<R>((((T) => R)) => Result<R, X>) mapValue;
	<R>((((X) => R)) => Result<T, R>) mapErr;
	<R>((((T) => R), ((X) => R)) => R) match;
	<R>(((() => Result<R, X>)) => Result<[T, R], X>) and;
	<R>(((() => Result<T, R>)) => Result<T, [X, R]>) or;
	(() => boolean) isOk;
	(() => Option<X>) findError;
}