import java.util.Optional;struct EmptyHead<T>{
	Optional<T> next(any* _ref_){
		return Optional.empty();
	}
	Head<T> Head(any* _ref_){
		return Head.new();
	}
}