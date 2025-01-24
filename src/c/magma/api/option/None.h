struct None<T>{
	<R>Option<R> map(Tuple<any*, R (*)(T)> mapper){
		return new None<>();
	}
	T orElseGet(Tuple<any*, T (*)()> other){
		return other.get();
	}
	Option<T> Option(){
		return Option.new();
	}
}