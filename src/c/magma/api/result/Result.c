import magma.api.Tuple;
import magma.api.option.Option;
import java.util.function.Function;
import java.util.function.Supplier;
public struct Result<T, X> {
	<R>(([Capture, ((Capture, T) => Result<R, X>)]) => Result<R, X>) flatMapValue;
	<R>(([Capture, ((Capture, T) => R)]) => Result<R, X>) mapValue;
	<R>(([Capture, ((Capture, X) => R)]) => Result<T, R>) mapErr;
	<R>(([Capture, ((Capture, T) => R)], [Capture, ((Capture, X) => R)]) => R) match;
	<R>(([Capture, ((Capture) => Result<R, X>)]) => Result<[T, R], X>) and;
	<R>(([Capture, ((Capture) => Result<T, R>)]) => Result<T, [X, Box<R>]>) or;
	(() => boolean) isOk;
	(() => Option<X>) findError;
}