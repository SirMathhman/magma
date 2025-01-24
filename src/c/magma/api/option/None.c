struct None<T>{
	<R>Option<R> map(R (*)(T) mapper){
		return new None<>();
	}
	T orElseGet(T (*)() other){
		return other.get();
	}
	Option<T> Option(){
		return Option.new();
	}
}