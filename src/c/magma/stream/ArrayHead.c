import magma.option.None;
import magma.option.Option;
import magma.option.Some;
struct ArrayHead<T> implements Head<T> {private final T value;private boolean retrieved;public ArrayHead(T value) {
        this.value = value;
        this.retrieved = false;
    }
	Option<T> next(){
		if (this.retrieved) {
			return None<>();
		}
		this.retrieved  = true;
		return Some<>(this.value);
	}
}