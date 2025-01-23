struct None<T> implements Option<T>{
	<R>Option<R> map([Any, ((void*, T) => R)] mapper){
		return None<>.new();
	}
	T orElseGet([Any, ((Any) => T)] other){
		return other.get();
	}
	struct None new(){
		struct None this;
		return this;
	}
}