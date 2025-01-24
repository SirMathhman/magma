struct Ok<T, X>(T value) implements Result<T, X>{
	<R>Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper){
		return mapper.apply(this.value);
	}
	<R>Result<R, X> mapValue(Function<T, R> mapper){
		return new Ok<>(mapper.apply(this.value));
	}
	<R>Result<T, R> mapErr(Function<X, R> mapper){
		return new Ok<>(this.value);
	}
	<R>R match(Function<T, R> onOk, Function<X, R> onErr){
		return onOk.apply(this.value);
	}
	<R>Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other){
		return other.get().mapValue(()->new Tuple<>(this.value, otherValue));
	}
	<R>Result<T, Tuple<X, R>> or(Supplier<Result<T, R>> other){
		return new Ok<>(this.value);
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
}
