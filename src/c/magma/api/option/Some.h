struct Some<T>(T value) implements Option<T>{
	<R>Option<R> map([Any, ((Any, T) => R)] mapper){
		return Some<>.new();
	}
	T orElseGet([Any, ((Any) => T)] other){
		return this.value;
	}
	struct Some new(){
		struct Some this;
		return this;
	}
}