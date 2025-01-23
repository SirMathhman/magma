struct Option<T>{
	struct VTable{
		<R>((((T) => R)) => Option<R>) map;
		(((() => T)) => T) orElseGet;
	}
	struct Option new(struct VTable table){
		struct Option this;
		return this;
	}
}