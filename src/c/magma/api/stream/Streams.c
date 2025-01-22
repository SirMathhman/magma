import java.util.ArrayList;import java.util.List;import java.util.Set;struct Streams{
	<T>Stream<T> of(T... values){
		return HeadedStream<>.new();
	}
	<T>Stream<T> from(List<T> list){
		return HeadedStream<>.new();
	}
	<T>Stream<T> from(Set<T> entries){
		return from(ArrayList<>.new());
	}
	Stream<Integer> reverse(String value){
		return HeadedStream<>.new();
	}
	<T>Stream<T> empty(){
		return HeadedStream<>.new();
	}
}