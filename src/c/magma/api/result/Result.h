import magma.api.Tuple;import magma.api.option.Option;struct Result<T, X>{
	<R>Result<R, X> flatMapValue(Tuple<any*, Result<R, X> (*)(T)> mapper);
	<R>Result<R, X> mapValue(Tuple<any*, R (*)(T)> mapper);
	<R>Result<T, R> mapErr(Tuple<any*, R (*)(X)> mapper);
	<R>R match(Tuple<any*, R (*)(T)> onOk, Tuple<any*, R (*)(X)> onErr);
	<R>Result<Tuple<T, R>, X> and(Tuple<any*, Result<R, X> (*)()> other);
	<R>Result<T, Tuple<X, R>> or(Tuple<any*, Result<T, R> (*)()> other);
	boolean isOk();
	Option<X> findError();
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}