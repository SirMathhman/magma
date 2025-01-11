import magma.option.None;
import magma.option.Option;
struct EmptyHead<T> implements Head<T> {
	Option<T> next(){
		return None<>();
	}
}