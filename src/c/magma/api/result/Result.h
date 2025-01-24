import magma.api.Tuple;import magma.api.option.Option;struct Result<T, X>{
	struct Table{
		<R>Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper);
		<R>Result<R, X> mapValue(Function<T, R> mapper);
		<R>Result<T, R> mapErr(Function<X, R> mapper);
		<R>R match(Function<T, R> onOk, Function<X, R> onErr);
		<R>Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other);
		<R>Result<T, Tuple<X, R>> or(Supplier<Result<T, R>> other);
		boolean isOk();
		Option<X> findError();
	}
	struct Impl{}
	struct Table table;
	struct Impl impl;
}