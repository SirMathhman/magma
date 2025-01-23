import magma.api.Tuple;import magma.api.option.None;import magma.api.option.Option;import magma.api.option.Some;import java.util.Optional;struct Err<T, X>(X error) implements Result<T, X>{
	<R>Result<R, X> flatMapValue([void*, Result<R, X> (*)(void*, T)] mapper){
		return Err<>.new();
	}
	<R>Result<R, X> mapValue([void*, R (*)(void*, T)] mapper){
		return Err<>.new();
	}
	<R>Result<T, R> mapErr([void*, R (*)(void*, X)] mapper){
		return Err<>.new();
	}
	<R>R match([void*, R (*)(void*, T)] onOk, [void*, R (*)(void*, X)] onErr){
		return onErr.apply(this.error);
	}
	<R>Result<Tuple<T, R>, X> and([void*, Result<R, X> (*)(void*)] other){
		return Err<>.new();
	}
	<R>Result<T, Tuple<X, R>> or([void*, Result<T, R> (*)(void*)] other){
		return other.get().mapErr(()->Tuple<>.new());
	}
	boolean isOk(){
		return false;
	}
	Optional<X> findError0(){
		return Optional.of(this.error);
	}
	Option<X> findError(){
		return findError0().<Option<X>>map(Some::new).orElseGet(None::new);
	}
	struct Err new(){
		struct Err this;
		return this;
	}
}