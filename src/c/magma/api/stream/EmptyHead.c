import java.util.Optional;

@Override
Optional<T> next(){
	return Optional.empty();
}
struct EmptyHead<T> implements Head<T> {
}

