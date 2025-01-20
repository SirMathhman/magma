import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
public struct Err<T, X>(X error) implements Result<T, X> {
	<R>((((T) => Result<R, X>)) => Result<R, X>) flatMapValue=<R>Result<R, X> flatMapValue(((T) => Result<R, X>) mapper){
		return new Err<>(this.error);
	};
	<R>((((T) => R)) => Result<R, X>) mapValue=<R>Result<R, X> mapValue(((T) => R) mapper){
		return new Err<>(this.error);
	};
	<R>((((X) => R)) => Result<T, R>) mapErr=<R>Result<T, R> mapErr(((X) => R) mapper){
		return new Err<>(mapper.apply(this.error));
	};
	<R>((((T) => R), ((X) => R)) => R) match=<R>R match(((T) => R) onOk, ((X) => R) onErr){
		return onErr.apply(this.error);
	};
	<R>(((() => Result<R, X>)) => Result<[T, R], X>) and=<R>Result<[T, R], X> and((() => Result<R, X>) other){
		return new Err<>(this.error);
	};
	<R>(((() => Result<T, R>)) => Result<T, [X, R]>) or=<R>Result<T, [X, R]> or((() => Result<T, R>) other){
		return other.get().mapErr(otherErr -> new Tuple<>(this.error, otherErr));
	};
	(() => boolean) isOk=boolean isOk(){
		return false;
	};
	(() => Optional<X>) findError0=Optional<X> findError0(){
		return Optional.of(this.error);
	};
	(() => Option<X>) findError=Option<X> findError(){
		return findError0().<Option<X>>map(Some::new).orElseGet(None::new);
	};
}