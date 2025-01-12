import magma.stream.Collector;
import magma.stream.HeadedStream;
import magma.stream.Stream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
struct SetCollector<T> implements Collector<T, JavaSet<T>> {
	JavaSet<T> createInitial(){
		return JavaSet<>();
	}
	JavaSet<T> fold(JavaSet<T> current, T next){
		return current.add(next);
	}}
}