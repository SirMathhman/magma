struct None<T> implements Option<T>{
	<R>Option<R> map([void*, ((void*, T) => R)] mapper){
		return None<>.new();
	}
	T orElseGet([void*, ((void*) => T)] other){
		return other.get();
	}
	struct None new(){
		struct None this;
		return this;
	}
}