import java.util.ArrayList;
import java.util.List;
import java.util.Set;
struct Streams{
	<T>Stream<T> of(T... values){
		return new HeadedStream<>(new RangeHead(values.length)).map(index -> values[index]);
	}
	<T>Stream<T> from(List<T> list){
		return new HeadedStream<>(new RangeHead(list.size())).map(list::get);
	}
	<T>Stream<T> from(Set<T> entries){
		return from(new ArrayList<>(entries));
	}
	Stream<Integer> reverse(String value){
		return new HeadedStream<>(new RangeHead(value.length())).map(index -> value.length() - index - 1);
	}
	<T>Stream<T> empty(){
		return new HeadedStream<>(new EmptyHead<>());
	}
}
