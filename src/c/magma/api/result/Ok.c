import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
public struct Ok<T, X>(T value) implements Result<T, X> {
	<R>((((T) => Result<R, X>)) => Result<R, X>) flatMapValue=<R>Result<R, X> flatMapValue(((T) => Result<R, X>) mapper){
		return mapper.apply(this.value);
	};
	<R>((((T) => R)) => Result<R, X>) mapValue=<R>Result<R, X> mapValue(((T) => R) mapper){
		return new Ok<>(mapper.apply(this.value));
	};
	<R>((((X) => R)) => Result<T, R>) mapErr=<R>Result<T, R> mapErr(((X) => R) mapper){
		return new Ok<>(this.value);
	};
	<R>((((T) => R), ((X) => R)) => R) match=<R>R match(((T) => R) onOk, ((X) => R) onErr){
		return onOk.apply(this.value);
	};
	<R>(((() => Result<R, X>)) => Result<Tuple<T, R>, X>) and=<R>Result<Tuple<T, R>, X> and((() => Result<R, X>) other){
		return other.get().mapValue(otherValue -> new Tuple<>(this.value, otherValue));
	};
	<R>(((() => Result<T, R>)) => Result<T, Tuple<X, R>>) or=<R>Result<T, Tuple<X, R>> or((() => Result<T, R>) other){
		return new Ok<>(this.value);
	};
	(() => boolean) isOk=boolean isOk(){
		return true;
	};
	(() => Optional<X>) findError0=Optional<X> findError0(){
		return Optional.empty();
	};
	(() => Option<X>) findError=Option<X> findError(){
		return findError0().<Option<X>>map(Some::new).orElseGet(None::new);
	};
}