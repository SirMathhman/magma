package magma.api.list;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.stream.Head;

public final class ListHead<T> implements Head<T> {
    private final List<T> list;
    private int counter = 0;

    public ListHead(List<T> list) {
        this.list = list;
    }

    @Override
    public Option<T> next() {
        if (counter >= list.size()) return new None<>();

        final var value = list.get(counter);
        counter++;
        return value;
    }
}
