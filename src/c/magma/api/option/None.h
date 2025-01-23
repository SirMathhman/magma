struct None<T> implements Option<T>{
	<R>Option<R> map([void*, R (*)(void*, T)] mapper){
		return None<>.new();
	}
	T orElseGet([void*, T (*)(void*)] other){
		return other.get();
	}
	struct None new(){
		struct None this;
		return this;
	}
}