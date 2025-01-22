struct None<T> implements Option<T>{
	<R>Option<R> map(((T) => R) mapper){
		return None<>.new();
	}
	T orElseGet((() => T) other){
		return other.get();
	}
}