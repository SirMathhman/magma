import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.stream.Collector;
import magma.stream.HeadedStream;
import magma.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
struct JavaList<T> {private final List<T> internal;public JavaList() {
        this(new ArrayList<>());
    }public JavaList(List<T> internal) {
        this.internal = internal;
    }
	JavaList<T>> collector(){
		return ListCollector<>();
	}
	JavaList<T> of(T... values){
		return JavaList<>(Arrays.asList(values));
	}
	boolean equals(Object o){
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JavaList<?> javaList = (JavaList<?>) o;
		return Objects.equals(this.internal, javaList.internal);
	}
	int hashCode(){
		return Objects.hashCode(this.internal);
	}
	JavaList<T> add(T next){
		this.internal.add(next);
		return this;
	}
	int size(){
		return this.internal.size();
	}
	Option<JavaList<T>> slice(int start, int end){
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