import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.stream.Collector;
import magma.stream.HeadedStream;
import magma.stream.Stream;
import java.util.ArrayList;
import java.util.List;
struct JavaList<T> {private final List<T> internal;public JavaList() {
        this(new ArrayList<>());
    }public JavaList(List<T> internal) {
        this.internal = internal;
    }
	JavaList<T>> collector(){
		return ListCollector<>();
	}
	JavaList<T> add(T next){
		this.internal.add(next);
		return this;
	}
	int size(){
		return this.internal.size();
	}
	Option<JavaList<T>> subList(int start, int end){
		if (start >= 0  &&  end >= 0  && start < size() && end < size() &&  end >= start) {
			return Some<>(JavaList<>(this.internal.subList(start, end)));
		}
		return None<>();
	}
	Stream<T> stream(){
		return HeadedStream<>(JavaListHead<>(this.internal));
	}
	JavaList<T> createInitial(){
		return JavaList<>();
		}

        @Override
        public JavaList<T> fold(JavaList<T> current, T next) {
            return current.add(next);}
	}
}