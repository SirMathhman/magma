struct Option<T>{
	<R>Option<R> map(R (*)(T) mapper);
	T orElseGet(T (*)() other);
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}