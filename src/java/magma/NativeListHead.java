package magma;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.List;

public final class NativeListHead<T> implements Head<T> {
    private final List<T> list;
    private int counter;

    public NativeListHead(List<T> list) {
        this.list = list;
    }

    @Override
    public Option<T> next() {
        if (counter < list.size()) {
            final var element = list.get(counter);
            counter++;
            return new Some<>(element);
        } else {
            return new None<>();
        }
    }
}
