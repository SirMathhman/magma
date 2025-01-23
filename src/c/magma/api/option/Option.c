struct Option<T>{
	struct VTable{
		<R>((((T) => R)) => Option<R>) map;
		(((() => T)) => T) orElseGet;
	}
	struct VTable vtable;
	struct Option new(struct VTable table){
		struct Option this;
		this.table=table;
		return this;
	}
}