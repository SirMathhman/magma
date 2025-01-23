struct Some<T>(T value) implements Option<T>{
	<R>Option<R> map([void*, R (*)(void*, T)] mapper){
		return Some<>.new();
	}
	T orElseGet([void*, T (*)(void*)] other){
		return this.value;
	}
	struct Some new(){
		struct Some this;
		return this;
	}
}