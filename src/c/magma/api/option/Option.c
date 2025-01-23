struct Option<T>{
	struct VTable{
		<R>((Any, [Any, ((Any, T) => R)]) => Option<R>) map;
		((Any, [Any, ((Any) => T)]) => T) orElseGet;
	}
	struct VTable vtable;
	struct Option new(struct VTable table){
		struct Option this;
		this.table=table;
		return this;
	}
}