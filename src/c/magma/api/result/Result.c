import magma.api.Tuple;import magma.api.option.Option;struct Result<T, X>{
	struct VTable{
		<R>((void*, [Any, ((void*, T) => Result<R, X>)]) => Result<R, X>) flatMapValue;
		<R>((void*, [Any, ((void*, T) => R)]) => Result<R, X>) mapValue;
		<R>((void*, [Any, ((void*, X) => R)]) => Result<T, R>) mapErr;
		<R>((void*, [Any, ((void*, T) => R)], [Any, ((void*, X) => R)]) => R) match;
		<R>((void*, [Any, ((Any) => Result<R, X>)]) => Result<Tuple<T, R>, X>) and;
		<R>((void*, [Any, ((Any) => Result<T, R>)]) => Result<T, Tuple<X, R>>) or;
		((void*) => boolean) isOk;
		((void*) => Option<X>) findError;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct Result new(Box<void*> ref, struct VTable vtable){
		struct Result this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}