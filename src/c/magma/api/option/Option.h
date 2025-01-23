struct Option<T>{
	struct VTable{
		<R>((Any, [Any, ((Any, T) => R)]) => Option<R>) map;
		((Any, [Any, ((Any) => T)]) => T) orElseGet;
	}
	Box<Any> ref;
	struct VTable vtable;
	struct Option new(Box<Any> ref, struct VTable vtable){
		struct Option this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}