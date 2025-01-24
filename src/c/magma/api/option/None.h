struct None<T>{
	<R>Option<R> map(Function<T, R> mapper){
		return new None<>();
	}
	T orElseGet(Supplier<T> other){
		return other.get();
	}
	Option<T> Option(){
		return Option.new();
	}
}