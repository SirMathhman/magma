import magma.api.Tuple;import magma.api.option.None;import magma.api.option.Option;import magma.api.option.Some;import java.util.Optional;struct Ok<T, X>(T value) implements Result<T, X>{
	<R>Result<R, X> flatMapValue([void*, Result<R, X> (*)(void*, T)] mapper){
		return mapper.apply(this.value);
	}
	<R>Result<R, X> mapValue([void*, R (*)(void*, T)] mapper){
		return Ok<>.new();
	}
	<R>Result<T, R> mapErr([void*, R (*)(void*, X)] mapper){
		return Ok<>.new();
	}
	<R>R match([void*, R (*)(void*, T)] onOk, [void*, R (*)(void*, X)] onErr){
		return onOk.apply(this.value);
	}
	<R>Result<Tuple<T, R>, X> and([void*, Result<R, X> (*)(void*)] other){
		return other.get().mapValue(()->Tuple<>.new());
	}
	<R>Result<T, Tuple<X, R>> or([void*, Result<T, R> (*)(void*)] other){
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