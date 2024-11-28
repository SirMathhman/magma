package magma.stream;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.List;

public class ListHead<T> implements Head<T> {
    private final List<T> list;
    private int counter;

    public ListHead(List<T> list) {
        this.list = list;
        this.counter = 0;
    }

    @Override
    public Option<T> next() {
        if (counter >= list.size()) return new None<>();

        final var element = list.get(counter);
        counter++;
        return new Some<>(element);
    }
}
