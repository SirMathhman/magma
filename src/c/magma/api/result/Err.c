import magma.api.Tuple;import magma.api.option.None;import magma.api.option.Option;import magma.api.option.Some;import java.util.Optional;struct Err<T, X>(X error) implements Result<T, X>{
	<R>Result<R, X> flatMapValue([void*, ((void*, T) => Result<R, X>)] mapper){
		return Err<>.new();
	}
	<R>Result<R, X> mapValue([void*, ((void*, T) => R)] mapper){
		return Err<>.new();
	}
	<R>Result<T, R> mapErr([void*, ((void*, X) => R)] mapper){
		return Err<>.new();
	}
	<R>R match([void*, ((void*, T) => R)] onOk, [void*, ((void*, X) => R)] onErr){
		return onErr.apply(this.error);
	}
	<R>Result<Tuple<T, R>, X> and([void*, ((void*) => Result<R, X>)] other){
		return Err<>.new();
	}
	<R>Result<T, Tuple<X, R>> or([void*, ((void*) => Result<T, R>)] other){
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