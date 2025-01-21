import java.util.Optional;
public struct EmptyHead<T> implements Head<T> {
	@Override
public Optional<T> next(){
	return Optional.empty();
}}

