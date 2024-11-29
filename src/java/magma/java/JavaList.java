package magma.java;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.HeadedStream;
import magma.api.stream.RangeHead;
import magma.api.stream.Stream;

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
        return new HeadedStream<>(new RangeHead(list.size())).map(list::get);
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

    public Stream<Tuple<Integer, T>> streamWithIndices() {
        return new HeadedStream<>(new RangeHead(list.size())).extend(list::get);
    }

    public Option<Stream<T>> sliceTo(int index) {
        if (index < list.size()) return new None<>();
        return new Some<>(new HeadedStream<>(new RangeHead(index))
                .map(list::get));
    }

    public Option<T> get(int index) {
        if(index < list.size()) return new Some<>(list.get(index));
        return new None<>();
    }
}
