import magma.api.Tuple;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
public struct Result<T, X> {
	<R> Result<R, X> flatMapValue(((T) => Result<R, X>) mapper);
	<R> Result<R, X> mapValue(((T) => R) mapper);
	<R> Result<T, R> mapErr(((X) => R) mapper);
	<R> R match(((T) => R) onOk,  ((X) => R) onErr);
	<R> Result<Tuple<T, R>, X> and();
	<R> Result<T, Tuple<X, R>> or();
	Optional<T> findValue();
	boolean isOk();
	Optional<X> findError();
}