package magma.java;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.stream.HeadedStream;
import magma.stream.ListHead;
import magma.stream.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record JavaList<T>(List<T> list) {
    public JavaList() {
        this(new ArrayList<>());
    }

    public JavaList<T> add(T node) {
        list.add(node);
        return this;
    }

    public Stream<T> stream() {
        return new HeadedStream<>(new ListHead<>(list));
    }

    public JavaList<T> addAll(JavaList<T> other) {
        list.addAll(other.list);
        return this;
    }

    public int size() {
        return list.size();
    }

    public Option<T> last() {
        if (list.isEmpty()) return new None<>();
        return new Some<>(list.getLast());
    }

    public Option<JavaList<T>> mapLast(Function<T, T> mapper) {
        return last().map(mapper).map(last -> set(list.size() - 1, last));
    }

    private JavaList<T> set(int index, T last) {
        list.set(index, last);
        return this;
    }
}
