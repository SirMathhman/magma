import magma.api.Tuple;import magma.api.option.Option;struct Result<T, X>{
	struct VTable{
		<R>((((T) => Result<R, X>)) => Result<R, X>) flatMapValue;
		<R>((((T) => R)) => Result<R, X>) mapValue;
		<R>((((X) => R)) => Result<T, R>) mapErr;
		<R>((((T) => R), ((X) => R)) => R) match;
		<R>(((() => Result<R, X>)) => Result<Tuple<T, R>, X>) and;
		<R>(((() => Result<T, R>)) => Result<T, Tuple<X, R>>) or;
		(() => boolean) isOk;
		(() => Option<X>) findError;
	}
	struct Result new(struct VTable table){
		struct Result this;
		return this;
	}
}