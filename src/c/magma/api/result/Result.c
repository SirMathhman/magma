import magma.api.Tuple;import magma.api.option.Option;struct Result<T, X>{
	<R>Result<R, X> flatMapValue(Tuple<any*, Result<R, X> (*)(any*, T)> mapper);
	<R>Result<R, X> mapValue(Tuple<any*, R (*)(any*, T)> mapper);
	<R>Result<T, R> mapErr(Tuple<any*, R (*)(any*, X)> mapper);
	<R>R match(Tuple<any*, R (*)(any*, T)> onOk, Tuple<any*, R (*)(any*, X)> onErr);
	<R>Result<Tuple<T, R>, X> and(Tuple<any*, Result<R, X> (*)(any*)> other);
	<R>Result<T, Tuple<X, R>> or(Tuple<any*, Result<T, R> (*)(any*)> other);
	boolean isOk();
	Option<X> findError();
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}