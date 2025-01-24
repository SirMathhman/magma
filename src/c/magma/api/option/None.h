struct None<T>{
	<R>Option<R> map(Tuple<any*, R (*)(any*, T)> mapper){
		return new None<>();
	}
	T orElseGet(Tuple<any*, T (*)(any*)> other){
		return other.get();
	}
	Option<T> Option(){
		return Option.new();
	}
}