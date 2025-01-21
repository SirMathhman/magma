import java.util.Optional;
 struct EmptyHead<T> implements Head<T> {
	@Override
 Optional<T> next(){
		return Optional.empty();
	}
}

