package magma.java;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.stream.Head;

import java.util.List;

public final class JavaListHead<T> implements Head<T> {
    private final List<T> list;
    private int counter = 0;

    public JavaListHead(List<T> list) {
        this.list = list;
    }

    @Override
    public Option<T> next() {
        if (this.counter >= this.list.size()) return new None<>();
        final var value = this.list.get(this.counter);
        this.counter++;
        return new Some<>(value);
    }
}
