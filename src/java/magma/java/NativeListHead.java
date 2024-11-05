package magma.java;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Head;

import java.util.List;

public final class NativeListHead<T> implements Head<T> {
    private final List<T> list;
    private int counter = 0;

    public NativeListHead(List<T> list) {
        this.list = list;
    }

    @Override
    public Option<T> next() {
        if (counter >= list.size()) return new None<>();

        final var element = list.get(counter);
        counter++;
        return new Some<>(element);
    }
}

