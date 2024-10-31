package magma.java;

import magma.core.option.None;
import magma.core.option.Option;
import magma.core.option.Some;
import magma.core.stream.HeadStream;
import magma.core.stream.Stream;

import java.util.ArrayList;
import java.util.List;

public record JavaList<T>(List<T> list) implements Indexed<T> {
    public JavaList() {
        this(new ArrayList<>());
    }

    public Stream<T> stream() {
        return new HeadStream<>(new IndexedHead<>(this));
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Option<T> get(int index) {
        if (index >= 0 && index < list.size()) {
            return new Some<>(list.get(index));
        } else {
            return new None<>();
        }
    }

    public JavaList<T> add(T value) {
        var copy = new ArrayList<>(list);
        copy.add(value);
        return new JavaList<>(copy);
    }
}
