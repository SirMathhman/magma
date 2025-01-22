struct None<T> implements Option<T>{
	<R>Option<R> map(((T) => R) mapper){
		return new None<>();
	}
	T orElseGet((() => T) other){
		return other.get();
	}
}