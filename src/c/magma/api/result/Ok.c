import magma.api.Tuple;import magma.api.option.None;import magma.api.option.Option;import magma.api.option.Some;import java.util.Optional;struct Ok<T, X>(T value) implements Result<T, X>{
	<R>Result<R, X> flatMapValue([Any, ((Any, T) => Result<R, X>)] mapper){
		return mapper.apply(this.value);
	}
	<R>Result<R, X> mapValue([Any, ((Any, T) => R)] mapper){
		return Ok<>.new();
	}
	<R>Result<T, R> mapErr([Any, ((Any, X) => R)] mapper){
		return Ok<>.new();
	}
	<R>R match([Any, ((Any, T) => R)] onOk, [Any, ((Any, X) => R)] onErr){
		return onOk.apply(this.value);
	}
	<R>Result<Tuple<T, R>, X> and([Any, ((Any) => Result<R, X>)] other){
		return other.get().mapValue(()->Tuple<>.new());
	}
	<R>Result<T, Tuple<X, R>> or([Any, ((Any) => Result<T, R>)] other){
		return Ok<>.new();
	}
	boolean isOk(){
		return true;
	}
	Optional<X> findError0(){
		return Optional.empty();
	}
	Option<X> findError(){
		return findError0().<Option<X>>map(Some::new).orElseGet(None::new);
	}
	struct Ok new(){
		struct Ok this;
		return this;
	}
}