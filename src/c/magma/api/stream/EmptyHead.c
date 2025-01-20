import java.util.Optional;
public struct EmptyHead<T> implements Head<T> {
	(() => Optional<T>) next=Optional<T> next(){
		return Optional.empty();
	};
}