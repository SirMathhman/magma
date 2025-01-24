struct Some<T>(T value){
	<R>Option<R> map(Tuple<any*, R (*)(any*, T)> mapper){
		return new Some<>(mapper.apply(this.value));
	}
	T orElseGet(Tuple<any*, T (*)(any*)> other){
		return this.value;
	}
	Option<T> Option(){
		return Option.new();
	}
}