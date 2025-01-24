struct Option<T>{
	<R>Option<R> map(Tuple<any*, R (*)(any*, T)> mapper);
	T orElseGet(Tuple<any*, T (*)(any*)> other);
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}