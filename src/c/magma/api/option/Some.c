struct Some<T>(T value) implements Option<T>{
	<R>Option<R> map(Function<T, R> mapper){
		return new Some<>(mapper.apply(this.value));
	}
	T orElseGet(Supplier<T> other){
		return this.value;
	}
	Option<T> Option(){
		return Option.new();
	}
}