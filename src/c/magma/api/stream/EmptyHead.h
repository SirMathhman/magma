import java.util.Optional;struct EmptyHead<T> implements Head<T>{
	Optional<T> next(){
		return Optional.empty();
	}
	Head<T> Head(){
		return Head.new();
	}
}