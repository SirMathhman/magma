struct Some<T>(T value) implements Option<T>{
	<R>Option<R> map([void*, ((void*, T) => R)] mapper){
		return Some<>.new();
	}
	T orElseGet([void*, ((void*) => T)] other){
		return this.value;
	}
	struct Some new(){
		struct Some this;
		return this;
	}
}