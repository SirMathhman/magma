import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.stream.Head;
import java.util.List;
struct JavaListHead<T> implements Head<T> {private final List<T> list;
	int counter = 0;public JavaListHead(List<T> list) {
        this.list = list;
    }
	Option<T> next(){
		if (this.counter >= this.list.size()) {
			return None<>();
		}
		auto value = this.list.get(this.counter);
		this.counter++;
		return Some<>(value);
	}
}