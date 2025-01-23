import java.util.Optional;struct EmptyHead<T> implements Head<T>{
	Optional<T> next(){
		return Optional.empty();
	}struct EmptyHead new(){struct EmptyHead this;return this;}
}