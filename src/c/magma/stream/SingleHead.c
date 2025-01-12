import magma.option.None;
import magma.option.Option;
import magma.option.Some;
struct SingleHead<T> implements Head<T> {private final T value;private boolean retrieved;public SingleHead(T value) {
        this.value = value;
        this.retrieved = false;
    }
	Option<T> next(){
		if (this.retrieved) {
			return None<>();
		}= true
		return Some<>(this.value);
	}
}