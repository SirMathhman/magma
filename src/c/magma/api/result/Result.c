import magma.api.Tuple;import magma.api.option.Option;struct Result<T, X>{
	<R>Result<R, X> flatMapValue(Result<R, X> (*)(T) mapper);
	<R>Result<R, X> mapValue(R (*)(T) mapper);
	<R>Result<T, R> mapErr(R (*)(X) mapper);
	<R>R match(R (*)(T) onOk, R (*)(X) onErr);
	<R>Result<Tuple<T, R>, X> and(Result<R, X> (*)() other);
	<R>Result<T, Tuple<X, R>> or(Result<T, R> (*)() other);
	boolean isOk();
	Option<X> findError();
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}