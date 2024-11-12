package magma.java;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.HeadedStream;
import magma.api.stream.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record JavaList<T>(List<T> list) {
    public JavaList() {
        this(Collections.emptyList());
    }

    public JavaList<T> add(T element) {
        var copy = new ArrayList<>(list());
        copy.add(element);
        return new JavaList<T>(copy);
    }

    public int size() {
        return list.size();
    }

    public Option<T> get(int index) {
        if (index >= 0 && index < list.size()) return new Some<>(list.get(index));
        else return new None<>();
    }

    public Stream<T> stream() {
        return new HeadedStream<>(new NativeListHead<>(list));
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
}