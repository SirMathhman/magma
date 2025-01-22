import java.util.ArrayList;import java.util.List;import java.util.Set;struct Streams{
	<T>Stream<T> of(T... values);
	<T>Stream<T> from(List<T> list);
	<T>Stream<T> from(Set<T> entries);
	Stream<Integer> reverse(String value);
	<T>Stream<T> empty();
}