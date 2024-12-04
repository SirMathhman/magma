package magma.stream.head;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.List;

public class ListHead<T> implements Head<T> {
    private final List<T> list;
    private int counter = 0;

    public ListHead(List<T> list) {
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
