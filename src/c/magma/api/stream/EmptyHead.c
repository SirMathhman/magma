import java.util.Optional;struct EmptyHead<T>{
	Optional<T> next(){
		return Optional.empty();
	}
	Head<T> Head(){
		return Head.new();
	}
}