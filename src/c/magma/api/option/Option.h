struct Option<T>{
	struct VTable{
		<R>((void*, [Any, ((void*, T) => R)]) => Option<R>) map;
		((void*, [Any, ((Any) => T)]) => T) orElseGet;
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