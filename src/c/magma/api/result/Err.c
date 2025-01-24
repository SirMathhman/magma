import magma.api.Tuple;import magma.api.option.None;import magma.api.option.Option;import magma.api.option.Some;import java.util.Optional;struct Err<T, X>(X error){
	<R>Result<R, X> flatMapValue(Tuple<any*, Result<R, X> (*)(any*, T)> mapper){
		return new Err<>(this.error);
	}
	<R>Result<R, X> mapValue(Tuple<any*, R (*)(any*, T)> mapper){
		return new Err<>(this.error);
	}
	<R>Result<T, R> mapErr(Tuple<any*, R (*)(any*, X)> mapper){
		return new Err<>(mapper.apply(this.error));
	}
	<R>R match(Tuple<any*, R (*)(any*, T)> onOk, Tuple<any*, R (*)(any*, X)> onErr){
		return onErr.apply(this.error);
	}
	<R>Result<Tuple<T, R>, X> and(Tuple<any*, Result<R, X> (*)(any*)> other){
		return new Err<>(this.error);
	}
	<R>Result<T, Tuple<X, R>> or(Tuple<any*, Result<T, R> (*)(any*)> other){
		return other.get().mapErr(()->new Tuple<>(this.error, otherErr));
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
	Result<T, X> Result(){
		return Result.new();
	}
}