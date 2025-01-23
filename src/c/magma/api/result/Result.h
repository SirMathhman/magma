import magma.api.Tuple;import magma.api.option.Option;struct Result<T, X>{
	struct VTable{
		<R>((void*, [void*, ((void*, T) => Result<R, X>)]) => Result<R, X>) flatMapValue;
		<R>((void*, [void*, ((void*, T) => R)]) => Result<R, X>) mapValue;
		<R>((void*, [void*, ((void*, X) => R)]) => Result<T, R>) mapErr;
		<R>((void*, [void*, ((void*, T) => R)], [void*, ((void*, X) => R)]) => R) match;
		<R>((void*, [void*, ((void*) => Result<R, X>)]) => Result<Tuple<T, R>, X>) and;
		<R>((void*, [void*, ((void*) => Result<T, R>)]) => Result<T, Tuple<X, R>>) or;
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