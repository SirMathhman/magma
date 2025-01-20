import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
public struct Ok<T, X>(T value) implements Result<T, X> {
	<R>(([Capture, ((Capture, T) => Result<R, X>)]) => Result<R, X>) flatMapValue=<R>Result<R, X> flatMapValue([Capture, ((Capture, T) => Result<R, X>)] mapper){
		return mapper.apply(this.value);
	};
	<R>(([Capture, ((Capture, T) => R)]) => Result<R, X>) mapValue=<R>Result<R, X> mapValue([Capture, ((Capture, T) => R)] mapper){
		return new Ok<>(mapper.apply(this.value));
	};
	<R>(([Capture, ((Capture, X) => R)]) => Result<T, R>) mapErr=<R>Result<T, R> mapErr([Capture, ((Capture, X) => R)] mapper){
		return new Ok<>(this.value);
	};
	<R>(([Capture, ((Capture, T) => R)], [Capture, ((Capture, X) => R)]) => R) match=<R>R match([Capture, ((Capture, T) => R)] onOk, [Capture, ((Capture, X) => R)] onErr){
		return onOk.apply(this.value);
	};
	<R>(([Capture, ((Capture) => Result<R, X>)]) => Result<[T, R], X>) and=<R>Result<[T, R], X> and([Capture, ((Capture) => Result<R, X>)] other){
		return other.get().mapValue(otherValue -> new Tuple<>(this.value, otherValue));
	};
	<R>(([Capture, ((Capture) => Result<T, R>)]) => Result<T, [X, R]>) or=<R>Result<T, [X, R]> or([Capture, ((Capture) => Result<T, R>)] other){
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