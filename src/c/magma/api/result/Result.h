import magma.api.Tuple;import magma.api.option.Option;struct Result<T, X>{
	struct VTable{
		<R>Result<R, X> (*)(void*, [void*, Result<R, X> (*)(void*, T)]) flatMapValue;
		<R>Result<R, X> (*)(void*, [void*, R (*)(void*, T)]) mapValue;
		<R>Result<T, R> (*)(void*, [void*, R (*)(void*, X)]) mapErr;
		<R>R (*)(void*, [void*, R (*)(void*, T)], [void*, R (*)(void*, X)]) match;
		<R>Result<Tuple<T, R>, X> (*)(void*, [void*, Result<R, X> (*)(void*)]) and;
		<R>Result<T, Tuple<X, R>> (*)(void*, [void*, Result<T, R> (*)(void*)]) or;
		boolean (*)(void*) isOk;
		Option<X> (*)(void*) findError;
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