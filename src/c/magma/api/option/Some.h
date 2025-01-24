struct Some<T>(T value){
	<R>Option<R> map(Tuple<any*, R (*)(T)> mapper){
		return new Some<>(mapper.apply(this.value));
	}
	T orElseGet(Tuple<any*, T (*)()> other){
		return this.value;
	}
	Option<T> Option(){
		return Option.new();
	}
}