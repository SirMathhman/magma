import magma.api.Tuple;import magma.api.option.None;import magma.api.option.Option;import magma.api.option.Some;import java.util.Optional;struct Ok<T, X>(T value){
	<R>Result<R, X> flatMapValue(any* _ref_, Tuple<any*, Result<R, X> (*)(any*, T)> mapper){
		return mapper.apply(this.value);
	}
	<R>Result<R, X> mapValue(any* _ref_, Tuple<any*, R (*)(any*, T)> mapper){
		return new Ok<>(mapper.apply(this.value));
	}
	<R>Result<T, R> mapErr(any* _ref_, Tuple<any*, R (*)(any*, X)> mapper){
		return new Ok<>(this.value);
	}
	<R>R match(any* _ref_, Tuple<any*, R (*)(any*, T)> onOk, Tuple<any*, R (*)(any*, X)> onErr){
		return onOk.apply(this.value);
	}
	<R>Result<Tuple<T, R>, X> and(any* _ref_, Tuple<any*, Result<R, X> (*)(any*)> other){
		return other.get().mapValue(()->new Tuple<>(this.value, otherValue));
	}
	<R>Result<T, Tuple<X, R>> or(any* _ref_, Tuple<any*, Result<T, R> (*)(any*)> other){
		return new Ok<>(this.value);
	}
	boolean isOk(any* _ref_){
		return true;
	}
	Optional<X> findError0(any* _ref_){
		return Optional.empty();
	}
	Option<X> findError(any* _ref_){
		return findError0().<Option<X>>map(Some::new).orElseGet(None::new);
	}
	Result<T, X> Result(any* _ref_){
		return Result.new();
	}
}