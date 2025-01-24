struct Some<T>(T value){
	<R>Option<R> map(R (*)(T) mapper){
		return new Some<>(mapper.apply(this.value));
	}
	T orElseGet(T (*)() other){
		return this.value;
	}
	Option<T> Option(){
		return Option.new();
	}
}