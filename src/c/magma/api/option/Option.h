struct Option<T>{
	<R>Option<R> map(any* _ref_, Tuple<any*, R (*)(any*, T)> mapper);
	T orElseGet(any* _ref_, Tuple<any*, T (*)(any*)> other);
	struct Impl{
		struct Impl new(any* _ref_){
			struct Impl this;
			return this;
		}
	}
}