import java.util.ArrayList;
import java.util.List;
import java.util.Set;
public struct Streams {
	<T>((T...) => Stream<T>) of=<T>Stream<T> of(T... values){
		return new HeadedStream<>(new RangeHead(values.length)).map(index -> values[index]);
	};
	<T>((List<T>) => Stream<T>) from=<T>Stream<T> from(List<T> list){
		return new HeadedStream<>(new RangeHead(list.size())).map(list::get);
	};
	<T>((Set<T>) => Stream<T>) from=<T>Stream<T> from(Set<T> entries){
		return from(new ArrayList<>(entries));
	};
	((String) => Stream<Integer>) reverse=Stream<Integer> reverse(String value){
		return new HeadedStream<>(new RangeHead(value.length())).map(index -> value.length() - index - 1);
	};
	<T>(() => Stream<T>) empty=<T>Stream<T> empty(){
		return new HeadedStream<>(new EmptyHead<>());
	};
}