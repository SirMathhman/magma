struct Option<T>{
	<R>Option<R> map(Function<T, R> mapper);
	T orElseGet(Supplier<T> other);
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}