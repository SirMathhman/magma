struct Option<T>{
	struct VTable{
		<R>Option<R> (*)(void*, [void*, R (*)(void*, T)]) map;
		T (*)(void*, [void*, T (*)(void*)]) orElseGet;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct Option new(Box<void*> ref, struct VTable vtable){
		struct Option this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}