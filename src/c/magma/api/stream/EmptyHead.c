import java.util.Optional;struct EmptyHead<T> implements Head<T>{
	struct Table{
		Optional<T> next(){
			return Optional.empty();
		}
	}
	struct Impl{}
	struct Table table;
	struct Impl impl;
}