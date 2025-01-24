struct EmptyHead<T> implements Head<T>{
	Option<T> next(){
		return new None<>();
	}
}
