struct Option<T>{
	<R>((((T) => R)) => Option<R>) map;
	(((() => T)) => T) orElseGet;
	struct Option new(){
		struct Option this;
		return this;
	}
}