struct Option<T>{
	<R>Option<R> map(Tuple<any*, R (*)(T)> mapper);
	T orElseGet(Tuple<any*, T (*)()> other);
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}