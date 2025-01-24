struct Some<T>(T value){
	<R>Option<R> map(any* _ref_, Tuple<any*, R (*)(any*, T)> mapper){
		return new Some<>(mapper.apply(this.value));
	}
	T orElseGet(any* _ref_, Tuple<any*, T (*)(any*)> other){
		return this.value;
	}
	Option<T> Option(){
		return Option.new();
	}
}