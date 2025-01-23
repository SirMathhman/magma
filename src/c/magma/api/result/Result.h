import magma.api.Tuple;import magma.api.option.Option;struct Result<T, X>{
	struct VTable{
		<R>((Any, [Any, ((Any, T) => Result<R, X>)]) => Result<R, X>) flatMapValue;
		<R>((Any, [Any, ((Any, T) => R)]) => Result<R, X>) mapValue;
		<R>((Any, [Any, ((Any, X) => R)]) => Result<T, R>) mapErr;
		<R>((Any, [Any, ((Any, T) => R)], [Any, ((Any, X) => R)]) => R) match;
		<R>((Any, [Any, ((Any) => Result<R, X>)]) => Result<Tuple<T, R>, X>) and;
		<R>((Any, [Any, ((Any) => Result<T, R>)]) => Result<T, Tuple<X, R>>) or;
		((Any) => boolean) isOk;
		((Any) => Option<X>) findError;
	}
	Box<Any> ref;
	struct VTable vtable;
	struct Result new(Box<Any> ref, struct VTable vtable){
		struct Result this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}