struct None<T>{
	<R>Option<R> map(any* _ref_, Tuple<any*, R (*)(any*, T)> mapper){
		return new None<>();
	}
	T orElseGet(any* _ref_, Tuple<any*, T (*)(any*)> other){
		return other.get();
	}
	Option<T> Option(any* _ref_){
		return Option.new();
	}
}